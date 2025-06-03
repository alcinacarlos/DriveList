package com.carlosalcina.drivelist.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.data.datasource.ImageStorageDataSource
import com.carlosalcina.drivelist.domain.model.CarColor
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.domain.repository.LocationRepository
import com.carlosalcina.drivelist.navigation.NavigationArgs
import com.carlosalcina.drivelist.ui.states.EditCarScreenState
import com.carlosalcina.drivelist.utils.KeywordGenerator
import com.carlosalcina.drivelist.utils.NetworkUtils
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class EditCarViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val savedStateHandle: SavedStateHandle,
    private val carListRepository: CarListRepository,
    private val carUploadRepository: CarUploadRepository,
    private val imageStorageDataSource: ImageStorageDataSource, // Inyectado para operaciones de Storage
    private val firebaseStorage: FirebaseStorage, // Para obtener referencia de Storage desde URL
    private val locationRepository: LocationRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditCarScreenState())
    val uiState: StateFlow<EditCarScreenState> = _uiState.asStateFlow()

    private val carIdToEdit: String? = savedStateHandle[NavigationArgs.CAR_ID_ARG]

    companion object {
        private const val TAG = "EditCarVM"
        internal const val MAX_IMAGES_ALLOWED = 10
    }

    init {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (carIdToEdit != null && currentUserId != null) {
            _uiState.update { it.copy(carIdToEdit = carIdToEdit) }
            loadInitialCarData(carIdToEdit, currentUserId)
            fetchBrandsForDropdown()
        } else {
            _uiState.update { it.copy(isLoadingInitialData = false, initialDataLoadError = "Error: No se pudo identificar el coche o el usuario.") }
            Log.e(TAG, "Car ID o User ID es nulo. CarID: $carIdToEdit, UserID: $currentUserId")
        }
    }

    private fun loadInitialCarData(carId: String, currentAuthUserId: String) {
        _uiState.update { it.copy(isLoadingInitialData = true, initialDataLoadError = null) }
        viewModelScope.launch {
            when (val result = carListRepository.getCarById(carId, currentAuthUserId)) {
                is Result.Success -> {
                    val car = result.data
                    if (car.userId != currentAuthUserId) {
                        _uiState.update { it.copy(isLoadingInitialData = false, canEditCar = false, initialDataLoadError = "No tienes permiso para editar este anuncio.") }
                        Log.w(TAG, "Permiso denegado: Usuario $currentAuthUserId intentando editar coche ${car.id} del usuario ${car.userId}")
                        return@launch
                    }
                    _uiState.update {
                        it.copy(
                            isLoadingInitialData = false,
                            canEditCar = true,
                            initialCarData = car,
                            brand = car.brand,
                            model = car.model,
                            bodyType = car.bodyType,
                            fuelType = car.fuelType,
                            year = car.year,
                            version = car.version,
                            selectedCarColor = CarColor.fromName(car.carColor),
                            price = car.price.toInt().toString(),
                            mileage = car.mileage.toString(),
                            description = car.description,
                            existingImageUrls = car.imageUrls,
                            finalComunidadAutonoma = car.comunidadAutonoma,
                            finalCiudad = car.ciudad,
                            finalPostalCode = car.postalCode,
                            manualLocationInput = car.ciudad ?: car.postalCode ?: car.comunidadAutonoma ?: ""
                        )
                    }
                    Log.d(TAG, "Datos iniciales del coche cargados: ${car.brand} ${car.model}")
                    if (car.brand.isNotBlank()) {
                        fetchModelsForDropdown(car.brand)
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoadingInitialData = false, initialDataLoadError = result.error.message ?: "Error al cargar datos del coche.") }
                    Log.e(TAG, "Error cargando datos iniciales: ${result.error.message}")
                }
            }
        }
    }

    // --- Actualizadores de campos del formulario ---
    fun onBrandSelected(brand: String?) { // Permitir null para limpiar
        _uiState.update { it.copy(brand = brand, model = null, availableModels = emptyList()) }
        brand?.let { fetchModelsForDropdown(it) }
    }
    fun onModelSelected(model: String?) { _uiState.update { it.copy(model = model) } } // Permitir null
    fun onBodyTypeChanged(bodyType: String) { _uiState.update { it.copy(bodyType = bodyType) } }
    fun onFuelTypeSelected(fuelType: String?) { _uiState.update { it.copy(fuelType = fuelType) } } // Permitir null
    fun onYearSelected(year: String) { _uiState.update { it.copy(year = year) } }
    fun onVersionChanged(version: String) { _uiState.update { it.copy(version = version) } }
    fun onCarColorSelected(color: CarColor?) { _uiState.update { it.copy(selectedCarColor = color) } } // Permitir null
    fun onPriceChanged(price: String) { _uiState.update { it.copy(price = price.filter { it.isDigit() }) } }
    fun onMileageChanged(mileage: String) { _uiState.update { it.copy(mileage = mileage.filter { it.isDigit() }) } }
    fun onDescriptionChanged(description: String) { _uiState.update { it.copy(description = description) } }

    // --- Lógica de Imágenes ---
    fun onNewImagesSelected(uris: List<Uri>) {
        _uiState.update {
            val currentTotalImages = it.existingImageUrls.size - it.imagesToDelete.size + it.selectedImageUris.size
            val canAddCount = (MAX_IMAGES_ALLOWED - currentTotalImages).coerceAtLeast(0)
            val newUrisToAdd = uris.take(canAddCount)
            it.copy(selectedImageUris = it.selectedImageUris + newUrisToAdd)
        }
    }
    fun removeSelectedNewImage(uri: Uri) {
        _uiState.update { it.copy(selectedImageUris = it.selectedImageUris - uri) }
    }
    fun toggleImageForDeletion(imageUrl: String) {
        _uiState.update {
            val currentToDelete = it.imagesToDelete.toMutableSet()
            if (currentToDelete.contains(imageUrl)) currentToDelete.remove(imageUrl)
            else currentToDelete.add(imageUrl)
            it.copy(imagesToDelete = currentToDelete)
        }
    }

    // --- Lógica de Localización (simplificada, asume que tienes la lógica en LocationRepository) ---
    fun onManualLocationInputChanged(input: String) { _uiState.update { it.copy(manualLocationInput = input, isManualLocationValid = true, locationValidationMessage = null) } }
    fun triggerLocationPermissionRequest() { /* ... (similar a UploadCarVM) ... */ }
    fun onLocationPermissionGranted() { fetchCurrentLocationAndPopulateInput() }
    fun onLocationPermissionDenied() { _uiState.update { it.copy(locationGeneralErrorMessage = "Permiso denegado.") } }

    private fun fetchCurrentLocationAndPopulateInput() {
        if (!NetworkUtils.isInternetAvailable(applicationContext)) {
            _uiState.update { it.copy(locationGeneralErrorMessage = "Sin conexión a internet.") }
            return
        }
        _uiState.update { it.copy(isFetchingLocationDetails = true, locationGeneralErrorMessage = null) }
        viewModelScope.launch {
            when (val locResult = locationRepository.getCurrentDeviceLocation()) {
                is Result.Success -> {
                    when (val addResult = locationRepository.getAddressFromCoordinates(locResult.data.first, locResult.data.second)) {
                        is Result.Success -> {
                            val address = addResult.data
                            _uiState.update { s ->
                                s.copy(
                                    isFetchingLocationDetails = false,
                                    finalCiudad = address.ciudad,
                                    finalComunidadAutonoma = address.comunidadAutonoma,
                                    finalPostalCode = address.postalCode,
                                    manualLocationInput = "${address.ciudad ?: ""}${if(address.ciudad !=null && address.comunidadAutonoma !=null) ", " else ""}${address.comunidadAutonoma ?: ""}".ifBlank { s.manualLocationInput },
                                    isManualLocationValid = true
                                )
                            }
                        }
                        is Result.Error -> _uiState.update { it.copy(isFetchingLocationDetails = false, locationValidationMessage = "No se pudo obtener la dirección.") }
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isFetchingLocationDetails = false, locationGeneralErrorMessage = "No se pudo obtener la ubicación.") }
            }
        }
    }
    fun searchManualLocation() {
        val query = _uiState.value.manualLocationInput.trim()
        if (query.isBlank()) {
            _uiState.update { it.copy(isManualLocationValid = false, locationValidationMessage = "Introduce un CP o ciudad.") }
            return
        }
        if (!NetworkUtils.isInternetAvailable(applicationContext)) {
            _uiState.update { it.copy(locationGeneralErrorMessage = "Sin conexión a internet.") }
            return
        }
        _uiState.update { it.copy(isFetchingLocationDetails = true, locationGeneralErrorMessage = null) }
        viewModelScope.launch {
            when (val addResult = locationRepository.getAddressFromQuery(query)) {
                is Result.Success -> {
                    val address = addResult.data
                    if (address.ciudad != null || address.comunidadAutonoma != null || address.postalCode != null) {
                        _uiState.update { s ->
                            s.copy(
                                isFetchingLocationDetails = false,
                                finalCiudad = address.ciudad,
                                finalComunidadAutonoma = address.comunidadAutonoma,
                                finalPostalCode = address.postalCode,
                                manualLocationInput = "${address.ciudad ?: ""}${if(address.ciudad !=null && address.comunidadAutonoma !=null) ", " else ""}${address.comunidadAutonoma ?: ""}".ifBlank { query },
                                isManualLocationValid = true
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isFetchingLocationDetails = false, isManualLocationValid = false, locationValidationMessage = "Ubicación no encontrada.") }
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isFetchingLocationDetails = false, isManualLocationValid = false, locationValidationMessage = "Error buscando ubicación.") }
            }
        }
    }


    // --- Carga de Desplegables ---
    private fun fetchBrandsForDropdown() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBrands = true, brandLoadError = null) }
            when(val result = carUploadRepository.getBrands()) {
                is Result.Success -> _uiState.update { it.copy(isLoadingBrands = false, availableBrands = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoadingBrands = false, brandLoadError = result.error.message ?: "Error cargando marcas") }
            }
        }
    }
    private fun fetchModelsForDropdown(brandName: String) {
        if (brandName.isBlank()) {
            _uiState.update { it.copy(availableModels = emptyList()) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingModels = true, modelLoadError = null) }
            when(val result = carUploadRepository.getModels(brandName)) {
                is Result.Success -> _uiState.update { it.copy(isLoadingModels = false, availableModels = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoadingModels = false, modelLoadError = result.error.message ?: "Error cargando modelos") }
            }
        }
    }

    // --- Guardar Cambios ---
    fun saveCarChanges() {
        val state = _uiState.value
        val carId = state.carIdToEdit ?: run {
            _uiState.update { it.copy(saveCarError = "Error: ID de coche no disponible.") }
            return
        }
        val currentAuthUserId = firebaseAuth.currentUser?.uid ?: run {
            _uiState.update { it.copy(saveCarError = "Error: Usuario no autenticado.") }
            return
        }
        if (state.initialCarData?.userId != currentAuthUserId) {
            _uiState.update { it.copy(saveCarError = "Error: No tienes permiso para editar este coche.") }
            return
        }

        // Validaciones
        if (state.brand.isNullOrBlank() || state.model.isNullOrBlank() || state.selectedCarColor == null ||
            state.price.isBlank() || state.mileage.isBlank() || state.description.isBlank() ||
            state.year.isBlank() || state.version.isBlank() || state.bodyType.isBlank() || state.fuelType.isNullOrBlank() ||
            (state.finalCiudad.isNullOrBlank() && state.finalComunidadAutonoma.isNullOrBlank() && state.finalPostalCode.isNullOrBlank()) ) {
            _uiState.update { it.copy(saveCarError = "Completa todos los campos obligatorios.") }
            return
        }
        val priceDouble = state.price.toDoubleOrNull()
        val mileageInt = state.mileage.toIntOrNull()
        if (priceDouble == null || priceDouble <= 0 || mileageInt == null || mileageInt < 0) {
            _uiState.update { it.copy(saveCarError = "Precio o kilometraje no válidos.") }
            return
        }

        _uiState.update { it.copy(isSavingCar = true, saveCarError = null) }

        viewModelScope.launch {
            val finalImageUrls = state.existingImageUrls.toMutableList()
            finalImageUrls.removeAll(state.imagesToDelete) // Eliminar las marcadas

            // 1. Eliminar imágenes de Storage
            if (state.imagesToDelete.isNotEmpty()) {
                _uiState.update { it.copy(imageProcessingMessage = "Eliminando imágenes antiguas...") }
                val deletionJobs = state.imagesToDelete.map { imageUrl ->
                    async {
                        try {
                            val storageRef = firebaseStorage.getReferenceFromUrl(imageUrl)
                            storageRef.delete().await()
                            Log.d(TAG, "Imagen eliminada de Storage: $imageUrl")
                            Result.Success(Unit)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error eliminando imagen de Storage: $imageUrl", e)
                            Result.Error(e) // No bloquear si una eliminación falla, pero loggear
                        }
                    }
                }
                deletionJobs.awaitAll() // Esperar a que todas las eliminaciones terminen (o fallen)
            }

            // 2. Subir nuevas imágenes
            if (state.selectedImageUris.isNotEmpty()) {
                _uiState.update { it.copy(isUploadingImages = true, imageProcessingMessage = "Subiendo nuevas imágenes...") }
                val uploadJobs = state.selectedImageUris.mapIndexed { index, uri ->
                    async {
                        val imageName = "image_edited_${index}_${System.currentTimeMillis()}.jpg"
                        val storagePath = "car_images/$carId/$imageName" // Usar el ID del coche existente
                        imageStorageDataSource.uploadImage(uri, storagePath)
                    }
                }
                val uploadResults = uploadJobs.awaitAll()
                var newUploadFailed = false
                uploadResults.forEach { result ->
                    when (result) {
                        is Result.Success -> finalImageUrls.add(result.data)
                        is Result.Error -> newUploadFailed = true
                    }
                }
                if (newUploadFailed) {
                    _uiState.update { it.copy(isSavingCar = false, isUploadingImages = false, imageProcessingMessage = null, saveCarError = "Error al subir una o más imágenes nuevas.") }
                    return@launch
                }
            }
            _uiState.update { it.copy(isUploadingImages = false, imageProcessingMessage = null) }


            // 3. Preparar y actualizar el coche
            val keywords = KeywordGenerator.generateKeywords(
                brand = state.brand!!, model = state.model!!, version = state.version,
                carColorName = state.selectedCarColor!!.name, fuelType = state.fuelType, year = state.year,
                ciudad = state.finalCiudad, comunidadAutonoma = state.finalComunidadAutonoma
            )

            val updatedCar = CarForSale(
                id = carId,
                userId = currentAuthUserId,
                brand = state.brand!!,
                model = state.model!!,
                bodyType = state.bodyType,
                fuelType = state.fuelType!!,
                year = state.year,
                version = state.version,
                carColor = state.selectedCarColor!!.name,
                price = priceDouble,
                mileage = mileageInt,
                description = state.description.trim(),
                imageUrls = finalImageUrls.distinct(),
                timestamp = state.initialCarData.timestamp,
                comunidadAutonoma = state.finalComunidadAutonoma?.takeIf { it.isNotBlank() },
                ciudad = state.finalCiudad?.takeIf { it.isNotBlank() },
                postalCode = state.finalPostalCode?.takeIf { it.isNotBlank() },
                searchableKeywords = keywords,
                isFavoriteByCurrentUser = state.initialCarData.isFavoriteByCurrentUser
            )

            when (val result = carUploadRepository.updateCar(updatedCar)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSavingCar = false, saveCarSuccess = true) }
                    Log.d(TAG, "Coche actualizado con éxito: $carId")
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSavingCar = false, saveCarError = result.error.message ?: "Error al guardar los cambios.") }
                    Log.e(TAG, "Error actualizando coche: ${result.error.message}")
                }
            }
        }
    }
}