package com.carlosalcina.drivelist.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.data.datasource.ImageStorageDataSource
import com.carlosalcina.drivelist.domain.model.CarColor
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.domain.repository.LocationRepository
import com.carlosalcina.drivelist.ui.states.UploadCarScreenState
import com.carlosalcina.drivelist.utils.KeywordGenerator
import com.carlosalcina.drivelist.utils.NetworkUtils
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UploadCarViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val uploadRepository: CarUploadRepository,
    private val firebaseAuth: FirebaseAuth,
    private val imageStorageDataSource: ImageStorageDataSource,
    private val locationRepository: LocationRepository
) : ViewModel() {

    companion object {
        const val MAX_IMAGES = 10
    }

    private val _uiState = MutableStateFlow(UploadCarScreenState())
    val uiState: StateFlow<UploadCarScreenState> = _uiState.asStateFlow()

    init {
        loadBrands()
    }

    fun loadBrands() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBrands = true, generalErrorMessage = null) }
            when (val result = uploadRepository.getBrands()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoadingBrands = false, brands = result.data)
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingBrands = false,
                            generalErrorMessage = "Error cargando marcas: ${result.error.message}"
                        )
                    }
                }
            }
        }
    }

    fun onImagesSelected(uris: List<Uri>) {
        _uiState.update {
            val currentCount = it.selectedImageUris.size
            val remainingSlots = MAX_IMAGES - currentCount
            val newUris = uris.take(remainingSlots)
            it.copy(selectedImageUris = it.selectedImageUris + newUris, imageUploadErrorMessage = null)
        }
    }

    fun removeSelectedImage(uri: Uri) {
        _uiState.update {
            it.copy(selectedImageUris = it.selectedImageUris.filterNot { it == uri })
        }
    }

    fun onCarColorSelected(colorOption: CarColor) {
        _uiState.update { it.copy(selectedCarColor = colorOption, generalErrorMessage = null) }
    }

    fun onBrandSelected(brand: String) {
        _uiState.update {
            it.copy(
                selectedBrand = brand,
                models = emptyList(), selectedModel = null, // Resetear cascada
                bodyTypes = emptyList(), selectedBodyType = null,
                fuelTypes = emptyList(), selectedFuelType = null,
                years = emptyList(), selectedYear = null,
                versions = emptyList(), selectedVersion = null,
                isLoadingModels = true, generalErrorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result = uploadRepository.getModels(brand)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingModels = false, models = result.data)
                }

                is Result.Error -> _uiState.update {
                    it.copy(
                        isLoadingModels = false,
                        generalErrorMessage = "Error cargando modelos: ${result.error.message}"
                    )
                }
            }
        }
    }

    fun onModelSelected(model: String) {
        val currentBrand =
            _uiState.value.selectedBrand ?: return
        _uiState.update {
            it.copy(
                selectedModel = model,
                bodyTypes = emptyList(), selectedBodyType = null, // Resetear cascada
                fuelTypes = emptyList(), selectedFuelType = null,
                years = emptyList(), selectedYear = null,
                versions = emptyList(), selectedVersion = null,
                isLoadingBodyTypes = true, generalErrorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result = uploadRepository.getBodyTypes(currentBrand, model)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingBodyTypes = false, bodyTypes = result.data)
                }

                is Result.Error -> _uiState.update {
                    it.copy(
                        isLoadingBodyTypes = false,
                        generalErrorMessage = "Error cargando carrocerías: ${result.error.message}"
                    )
                }
            }
        }
    }

    fun onBodyTypeSelected(bodyType: String) {
        val state = _uiState.value
        val (currentBrand, currentModel) = state.selectedBrand to state.selectedModel
        if (currentBrand == null || currentModel == null) return

        _uiState.update {
            it.copy(
                selectedBodyType = bodyType,
                fuelTypes = emptyList(), selectedFuelType = null, // Resetear cascada
                years = emptyList(), selectedYear = null,
                versions = emptyList(), selectedVersion = null,
                isLoadingFuelTypes = true, generalErrorMessage = null
            )
        }
        viewModelScope.launch {
            val result = uploadRepository.getFuelTypes(currentBrand, currentModel, bodyType)
            when (result) {
                is Result.Success -> _uiState.update {
                    Log.d("UploadCarViewModel", "getFuelTypes result: ${result.data}")
                    it.copy(isLoadingFuelTypes = false, fuelTypes = result.data)
                }

                is Result.Error -> _uiState.update {
                    it.copy(
                        isLoadingFuelTypes = false,
                        generalErrorMessage = "Error cargando combustibles: ${result.error.message}"
                    )
                }
            }
        }
    }

    fun onFuelTypeSelected(fuelType: String) {
        val state = _uiState.value
        val (currentBrand, currentModel, currentBodyType) = Triple(
            state.selectedBrand,
            state.selectedModel,
            state.selectedBodyType
        )
        if (currentBrand == null || currentModel == null || currentBodyType == null) return

        _uiState.update {
            it.copy(
                selectedFuelType = fuelType,
                years = emptyList(), selectedYear = null, // Resetear cascada
                versions = emptyList(), selectedVersion = null,
                isLoadingYears = true, generalErrorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result =
                uploadRepository.getYears(currentBrand, currentModel, currentBodyType, fuelType)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingYears = false, years = result.data)
                }

                is Result.Error -> _uiState.update {
                    it.copy(
                        isLoadingYears = false,
                        generalErrorMessage = "Error cargando años: ${result.error.message}"
                    )
                }
            }
        }
    }

    fun onYearSelected(year: String) {
        val state = _uiState.value
        val currentBrand = state.selectedBrand
        val currentModel = state.selectedModel
        val currentBodyType = state.selectedBodyType
        val currentFuelType = state.selectedFuelType

        if (currentBrand == null || currentModel == null || currentBodyType == null || currentFuelType == null) return

        _uiState.update {
            it.copy(
                selectedYear = year,
                versions = emptyList(), selectedVersion = null, // Resetear cascada
                isLoadingVersions = true, generalErrorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result = uploadRepository.getVersions(
                currentBrand,
                currentModel,
                currentBodyType,
                currentFuelType,
                year
            )) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingVersions = false, versions = result.data)
                }

                is Result.Error -> _uiState.update {
                    it.copy(
                        isLoadingVersions = false,
                        generalErrorMessage = "Error cargando versiones: ${result.error.message}"
                    )
                }
            }
        }
    }

    fun onVersionSelected(version: String) {
        _uiState.update { it.copy(selectedVersion = version, generalErrorMessage = null) }
    }

    // Funciones para actualizar los campos de texto
    fun onPriceChanged(price: String) {
        _uiState.update { it.copy(price = price) }
    }

    fun onMileageChanged(mileage: String) {
        _uiState.update { it.copy(mileage = mileage) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onManualLocationInputChanged(input: String) {
        _uiState.update {
            it.copy(
                manualLocationInput = input,
                isManualLocationValid = true, // Resetear validación al escribir
                locationValidationMessage = null
            )
        }
    }

    fun triggerLocationPermissionRequest() {
        _uiState.update { it.copy(isRequestingLocationPermission = true, locationGeneralErrorMessage = null) }
    }

    fun onLocationPermissionGranted() {
        _uiState.update { it.copy(isRequestingLocationPermission = false) } // Resetear flag
        fetchCurrentLocationAndPopulateInput()
    }

    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(
                isRequestingLocationPermission = false, // Resetear flag
                locationGeneralErrorMessage = "Permiso denegado. Introduce la ubicación manualmente."
            )
        }
    }

    internal fun fetchCurrentLocationAndPopulateInput() {
        _uiState.update { it.copy(isFetchingLocationDetails = true, locationGeneralErrorMessage = null, isManualLocationValid = true, locationValidationMessage = null) }
        viewModelScope.launch {
            when (val locationResult = locationRepository.getCurrentDeviceLocation()) {
                is Result.Success -> {
                    val (lat, lon) = locationResult.data
                    when (val addressResult = locationRepository.getAddressFromCoordinates(lat, lon)) {
                        is Result.Success -> {
                            val address = addressResult.data
                            _uiState.update {
                                it.copy(
                                    isFetchingLocationDetails = false,
                                    finalCiudad = address.ciudad,
                                    finalComunidadAutonoma = address.comunidadAutonoma,
                                    finalPostalCode = address.postalCode,
                                    // Sobreescribir el campo de input con la ciudad y comunidad (o lo que prefieras)
                                    manualLocationInput = "${address.ciudad ?: ""}${if (address.ciudad != null && address.comunidadAutonoma != null) ", " else ""}${address.comunidadAutonoma ?: ""}".trim(',',' '),
                                    isManualLocationValid = true,
                                    locationValidationMessage = null
                                )
                            }
                        }
                        is Result.Error -> { // Error de Geocoding
                            _uiState.update {
                                it.copy(
                                    isFetchingLocationDetails = false,
                                    locationGeneralErrorMessage = "No se pudo obtener la dirección desde las coordenadas.",
                                    // No invalidar el campo de input necesariamente por esto
                                )
                            }
                        }
                    }
                }
                is Result.Error -> { // Error al obtener lat/lon
                    _uiState.update {
                        it.copy(
                            isFetchingLocationDetails = false,
                            locationGeneralErrorMessage = "No se pudo obtener la localización actual: ${locationResult.error.message}"
                        )
                    }
                }
            }
        }
    }

    fun searchManualLocation() {
        val query = _uiState.value.manualLocationInput.trim()
        if (query.isBlank()) {
            _uiState.update { it.copy(isManualLocationValid = false, locationValidationMessage = "El campo no puede estar vacío.") }
            return
        }

        _uiState.update { it.copy(isFetchingLocationDetails = true, locationGeneralErrorMessage = null, isManualLocationValid = true, locationValidationMessage = null) }
        viewModelScope.launch {
            when (val addressResult = locationRepository.getAddressFromQuery(query)) {
                is Result.Success -> {
                    val address = addressResult.data
                    if (address.ciudad != null || address.comunidadAutonoma != null || address.postalCode != null) {
                        _uiState.update {
                            it.copy(
                                isFetchingLocationDetails = false,
                                finalCiudad = address.ciudad,
                                finalComunidadAutonoma = address.comunidadAutonoma,
                                finalPostalCode = address.postalCode,
                                // Actualizar el input para mostrar lo encontrado de forma formateada y validada
                                manualLocationInput = "${address.ciudad ?: ""}${if (address.ciudad != null && address.comunidadAutonoma != null) ", " else ""}${address.comunidadAutonoma ?: ""}".ifBlank { query },
                                isManualLocationValid = true,
                                locationValidationMessage = null
                            )
                        }
                    } else { // La API devolvió éxito pero sin datos útiles (lista vacía)
                        _uiState.update {
                            it.copy(
                                isFetchingLocationDetails = false,
                                finalCiudad = null, // Limpiar datos anteriores si la búsqueda falla
                                finalComunidadAutonoma = null,
                                finalPostalCode = null,
                                isManualLocationValid = false,
                                locationValidationMessage = "Ubicación no encontrada para '$query'."
                            )
                        }
                    }
                }
                is Result.Error -> { // Error de la API GeoNames
                    _uiState.update {
                        it.copy(
                            isFetchingLocationDetails = false,
                            finalCiudad = null,
                            finalComunidadAutonoma = null,
                            finalPostalCode = null,
                            isManualLocationValid = false,
                            locationValidationMessage = "Error al buscar '$query': Inténtalo de nuevo."
                        )
                    }
                }
            }
        }
    }

    fun prepareAndSubmitCar() {
        val state = _uiState.value
        val userId = firebaseAuth.currentUser?.uid

        if (!NetworkUtils.isInternetAvailable(applicationContext)) {
            _uiState.update {
                it.copy(
                    isUploadingImages = false, // Ensure these are reset
                    formUploadInProgress = false,
                    generalErrorMessage = "No internet connection. Please check your connection and try again."
                )
            }
            return // Stop the process
        }

        if (userId == null) {
            _uiState.update { it.copy(generalErrorMessage = "Error: Usuario no autenticado.") }
            return
        }

        if (state.selectedBrand == null || state.selectedModel == null ||
            state.selectedBodyType == null || state.selectedFuelType == null ||
            state.selectedYear == null || state.selectedVersion == null ||
            state.selectedCarColor == null ||
            state.price.isBlank() || state.mileage.isBlank() || state.description.isBlank()
        ) {
            _uiState.update { it.copy(generalErrorMessage = "Por favor, completa todos los campos del formulario.") }
            return
        }
        val priceDouble = state.price.toDoubleOrNull()
        val mileageInt = state.mileage.toIntOrNull()

        if (priceDouble == null || priceDouble <= 0) {
            _uiState.update { it.copy(generalErrorMessage = "El precio no es válido.") }
            return
        }
        if (mileageInt == null || mileageInt < 0) {
            _uiState.update { it.copy(generalErrorMessage = "El kilometraje no es válido.") }
            return
        }

        if (state.finalCiudad.isNullOrBlank() && state.finalComunidadAutonoma.isNullOrBlank()) {
            // Si no hay datos finales Y el input manual no es válido Y no se está buscando, mostrar error.
            if (state.manualLocationInput.isNotBlank() && !state.isManualLocationValid && !state.isFetchingLocationDetails) {
                _uiState.update { it.copy(generalErrorMessage = "La ubicación introducida no es válida. Por favor, corrígela o usa la detección automática.") }
                return
            } else if (state.manualLocationInput.isBlank()) {
                _uiState.update { it.copy(generalErrorMessage = "Por favor, proporciona una ubicación para el vehículo.") }
                return
            }
        }


        _uiState.update {
            it.copy(
                isUploadingImages = true,
                formUploadInProgress = true,
                imageUploadErrorMessage = null,
                generalErrorMessage = null
            )
        }

        viewModelScope.launch {
            val carId = UUID.randomUUID().toString()
            val uploadedImageUrls = mutableListOf<String>()

            if (state.selectedImageUris.isNotEmpty()) {
                val uploadJobs = state.selectedImageUris.mapIndexed { index, uri ->
                    async {
                        val imageName =
                            "image_${index}_${System.currentTimeMillis()}.jpg"
                        val storagePath = "car_images/$carId/$imageName"
                        imageStorageDataSource.uploadImage(uri, storagePath)
                    }
                }

                val results = uploadJobs.awaitAll()

                var uploadFailed = false
                results.forEach { result ->
                    when (result) {
                        is Result.Success -> uploadedImageUrls.add(result.data)
                        is Result.Error -> {
                            uploadFailed = true
                        }
                    }
                }

                if (uploadFailed) {
                    _uiState.update {
                        it.copy(
                            isUploadingImages = false,
                            formUploadInProgress = false,
                            imageUploadErrorMessage = "Error al subir una o más imágenes. Inténtalo de nuevo."
                        )
                    }
                    return@launch // No continuar si la subida de imágenes falló
                }
            }
            _uiState.update { it.copy(isUploadingImages = false) }

            val keywords = KeywordGenerator.generateKeywords(
                brand = state.selectedBrand,
                model = state.selectedModel,
                version = state.selectedVersion,
                carColorName = state.selectedCarColor.name,
                fuelType = state.selectedFuelType,
                year = state.selectedYear,
                ciudad = state.finalCiudad,
                comunidadAutonoma = state.finalComunidadAutonoma,
            )

            val carToUpload = CarForSale(
                id = carId, // Usa el ID generado
                userId = userId,
                brand = state.selectedBrand,
                model = state.selectedModel,
                bodyType = state.selectedBodyType,
                fuelType = state.selectedFuelType,
                year = state.selectedYear,
                version = state.selectedVersion,
                carColor = state.selectedCarColor.name,
                price = priceDouble,
                mileage = mileageInt,
                description = state.description.trim(),
                imageUrls = uploadedImageUrls,
                comunidadAutonoma = state.finalComunidadAutonoma?.takeIf { it.isNotBlank() },
                ciudad = state.finalCiudad?.takeIf { it.isNotBlank() },
                postalCode = state.finalPostalCode?.takeIf { it.isNotBlank() },
                searchableKeywords = keywords
            )

            // Subir datos del coche a Firestore
            when (val formResult = uploadRepository.uploadCar(carToUpload)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            formUploadInProgress = false,
                            formUploadSuccess = true,
                            // Limpiar formulario después del éxito
                            selectedImageUris = emptyList(),
                            price = "", mileage = "", description = "",
                            selectedBrand = null, selectedModel = null, selectedBodyType = null,
                            selectedFuelType = null, selectedYear = null, selectedVersion = null,
                            brands = emptyList() // Podrías recargar marcas o dejar que se recarguen al reentrar
                        )
                    }
                    loadBrands()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            formUploadInProgress = false,
                            formUploadSuccess = false,
                            generalErrorMessage = "Error al subir los datos del coche: ${formResult.error.message}"
                        )
                    }
                }
            }
        }
    }


    fun resetFormUploadStatus() {
        _uiState.update {
            it.copy(
                formUploadSuccess = false,
                generalErrorMessage = null,
                formUploadInProgress = false,
                imageUploadErrorMessage = null,
                isUploadingImages = false
            )
        }
    }
}