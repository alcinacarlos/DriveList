package com.carlosalcina.drivelist.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.data.datasource.ImageStorageDataSource
import com.carlosalcina.drivelist.domain.model.CarColor
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.domain.repository.LocationRepository
import com.carlosalcina.drivelist.ui.states.EditCarScreenState
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
import javax.inject.Inject

@HiltViewModel
class EditCarViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val carRepository: CarListRepository,
    private val uploadRepository: CarUploadRepository,
    private val firebaseAuth: FirebaseAuth,
    private val imageStorageDataSource: ImageStorageDataSource,
    private val locationRepository: LocationRepository
) : ViewModel() {

    companion object {
        const val MAX_IMAGES = 10
    }

    private val _uiState = MutableStateFlow(EditCarScreenState())
    val uiState: StateFlow<EditCarScreenState> = _uiState.asStateFlow()
    val curentUser = firebaseAuth.currentUser

    fun loadCarDetails(carId: String) {
        if (carId.isBlank()) {
            _uiState.update { it.copy(generalErrorMessage = "ID de coche no válido.") }
            return
        }
        _uiState.update { it.copy(isLoadingCarDetails = true, carId = carId) }

        viewModelScope.launch {
            val carDetailsJob = async { carRepository.getCarById(carId, curentUser?.uid) }
            val brandsJob = async { uploadRepository.getBrands() }

            val carResult = carDetailsJob.await()
            val brandsResult = brandsJob.await()

            if (brandsResult is Result.Success) {
                _uiState.update { it.copy(brands = brandsResult.data) }
            } else if (brandsResult is Result.Error) {
                _uiState.update {
                    it.copy(
                        isLoadingCarDetails = false,
                        generalErrorMessage = "Error cargando marcas: ${brandsResult.error.message}"
                    )
                }
                return@launch
            }

            when (carResult) {
                is Result.Success -> {
                    val car = carResult.data
                    populateStateWithCarData(car)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingCarDetails = false,
                            generalErrorMessage = "Error cargando los detalles del coche: ${carResult.error.message}"
                        )
                    }
                }
            }
        }
    }

    private fun populateStateWithCarData(car: CarForSale) {
        _uiState.update {
            it.copy(
                price = car.price.toString(),
                mileage = car.mileage.toString(),
                description = car.description,
                existingImageUrls = car.imageUrls,
                selectedCarColor = CarColor.fromName(car.carColor),
                manualLocationInput = "${car.ciudad ?: ""}, ${car.postalCode ?: ""}".trim(',', ' ')
                    .trim(),
                finalCiudad = car.ciudad,
                finalComunidadAutonoma = car.comunidadAutonoma,
                finalPostalCode = car.postalCode,
                selectedBrand = car.brand,
                selectedModel = car.model,
                selectedBodyType = car.bodyType,
                selectedFuelType = car.fuelType,
                selectedYear = car.year,
                selectedVersion = car.version,
                isLoadingCarDetails = false
            )
        }
        loadDependentDropdowns(car)
    }

    private fun loadDependentDropdowns(car: CarForSale) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingModels = true) }
            uploadRepository.getModels(car.brand).let { result ->
                if (result is Result.Success) _uiState.update {
                    it.copy(
                        models = result.data, isLoadingModels = false
                    )
                }
            }

            _uiState.update { it.copy(isLoadingBodyTypes = true) }
            uploadRepository.getBodyTypes(car.brand, car.model).let { result ->
                if (result is Result.Success) _uiState.update {
                    it.copy(
                        bodyTypes = result.data, isLoadingBodyTypes = false
                    )
                }
            }

            _uiState.update { it.copy(isLoadingFuelTypes = true) }
            uploadRepository.getFuelTypes(car.brand, car.model, car.bodyType).let { result ->
                if (result is Result.Success) _uiState.update {
                    it.copy(
                        fuelTypes = result.data, isLoadingFuelTypes = false
                    )
                }
            }

            _uiState.update { it.copy(isLoadingYears = true) }
            uploadRepository.getYears(car.brand, car.model, car.bodyType, car.fuelType)
                .let { result ->
                    if (result is Result.Success) _uiState.update {
                        it.copy(
                            years = result.data, isLoadingYears = false
                        )
                    }
                }

            _uiState.update { it.copy(isLoadingVersions = true) }
            uploadRepository.getVersions(car.brand, car.model, car.bodyType, car.fuelType, car.year)
                .let { result ->
                    if (result is Result.Success) _uiState.update {
                        it.copy(
                            versions = result.data, isLoadingVersions = false
                        )
                    }
                }
        }
    }

    fun onImagesSelected(uris: List<Uri>) {
        _uiState.update {
            val currentCount = it.existingImageUrls.size + it.newSelectedImageUris.size
            val remainingSlots = MAX_IMAGES - currentCount
            val newUris = uris.take(remainingSlots)
            it.copy(
                newSelectedImageUris = it.newSelectedImageUris + newUris,
                imageUploadErrorMessage = null
            )
        }
    }

    fun removeNewSelectedImage(uri: Uri) {
        _uiState.update {
            it.copy(newSelectedImageUris = it.newSelectedImageUris.filterNot { item -> item == uri })
        }
    }

    fun removeExistingImage(url: String) {
        _uiState.update {
            it.copy(existingImageUrls = it.existingImageUrls.filterNot { item -> item == url })
        }
    }

    fun onCarColorSelected(colorOption: CarColor) {
        _uiState.update { it.copy(selectedCarColor = colorOption, generalErrorMessage = null) }
    }

    fun onBrandSelected(brand: String) {
        _uiState.update {
            it.copy(
                selectedBrand = brand,
                models = emptyList(),
                selectedModel = null,
                bodyTypes = emptyList(),
                selectedBodyType = null,
                fuelTypes = emptyList(),
                selectedFuelType = null,
                years = emptyList(),
                selectedYear = null,
                versions = emptyList(),
                selectedVersion = null,
                isLoadingModels = true,
                generalErrorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result = uploadRepository.getModels(brand)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoadingModels = false, models = result.data
                    )
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
        val currentBrand = _uiState.value.selectedBrand ?: return
        _uiState.update {
            it.copy(
                selectedModel = model,
                bodyTypes = emptyList(),
                selectedBodyType = null,
                fuelTypes = emptyList(),
                selectedFuelType = null,
                years = emptyList(),
                selectedYear = null,
                versions = emptyList(),
                selectedVersion = null,
                isLoadingBodyTypes = true,
                generalErrorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result = uploadRepository.getBodyTypes(currentBrand, model)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoadingBodyTypes = false, bodyTypes = result.data
                    )
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
                fuelTypes = emptyList(),
                selectedFuelType = null,
                years = emptyList(),
                selectedYear = null,
                versions = emptyList(),
                selectedVersion = null,
                isLoadingFuelTypes = true,
                generalErrorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result =
                uploadRepository.getFuelTypes(currentBrand, currentModel, bodyType)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoadingFuelTypes = false, fuelTypes = result.data
                    )
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
            state.selectedBrand, state.selectedModel, state.selectedBodyType
        )
        if (currentBrand == null || currentModel == null || currentBodyType == null) return

        _uiState.update {
            it.copy(
                selectedFuelType = fuelType,
                years = emptyList(),
                selectedYear = null,
                versions = emptyList(),
                selectedVersion = null,
                isLoadingYears = true,
                generalErrorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result =
                uploadRepository.getYears(currentBrand, currentModel, currentBodyType, fuelType)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoadingYears = false, years = result.data
                    )
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
                versions = emptyList(),
                selectedVersion = null,
                isLoadingVersions = true,
                generalErrorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result = uploadRepository.getVersions(
                currentBrand, currentModel, currentBodyType, currentFuelType, year
            )) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoadingVersions = false, versions = result.data
                    )
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
                isManualLocationValid = true,
                locationValidationMessage = null
            )
        }
    }

    fun triggerLocationPermissionRequest() {
        _uiState.update {
            it.copy(
                isRequestingLocationPermission = true, locationGeneralErrorMessage = null
            )
        }
    }

    fun onLocationPermissionGranted() {
        _uiState.update { it.copy(isRequestingLocationPermission = false) }
        fetchCurrentLocationAndPopulateInput()
    }

    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(
                isRequestingLocationPermission = false,
                locationGeneralErrorMessage = "Permiso denegado. Introduce la ubicación manualmente."
            )
        }
    }

    fun fetchCurrentLocationAndPopulateInput() {
        _uiState.update {
            it.copy(
                isFetchingLocationDetails = true,
                locationGeneralErrorMessage = null,
                isManualLocationValid = true,
                locationValidationMessage = null
            )
        }
        viewModelScope.launch {
            when (val locationResult = locationRepository.getCurrentDeviceLocation()) {
                is Result.Success -> {
                    val (lat, lon) = locationResult.data
                    when (val addressResult =
                        locationRepository.getAddressFromCoordinates(lat, lon)) {
                        is Result.Success -> {
                            val address = addressResult.data
                            _uiState.update {
                                it.copy(
                                    isFetchingLocationDetails = false,
                                    finalCiudad = address.ciudad,
                                    finalComunidadAutonoma = address.comunidadAutonoma,
                                    finalPostalCode = address.postalCode,
                                    manualLocationInput = "${address.ciudad ?: ""}${if (address.ciudad != null && address.comunidadAutonoma != null) ", " else ""}${address.comunidadAutonoma ?: ""}".trim(
                                        ',', ' '
                                    ),
                                    isManualLocationValid = true,
                                    locationValidationMessage = null
                                )
                            }
                        }

                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isFetchingLocationDetails = false,
                                    locationGeneralErrorMessage = "No se pudo obtener la dirección desde las coordenadas."
                                )
                            }
                        }
                    }
                }

                is Result.Error -> {
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
            _uiState.update {
                it.copy(
                    isManualLocationValid = false,
                    locationValidationMessage = "El campo no puede estar vacío."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isFetchingLocationDetails = true,
                locationGeneralErrorMessage = null,
                isManualLocationValid = true,
                locationValidationMessage = null
            )
        }
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
                                manualLocationInput = "${address.ciudad ?: ""}${if (address.ciudad != null && address.comunidadAutonoma != null) ", " else ""}${address.comunidadAutonoma ?: ""}".ifBlank { query },
                                isManualLocationValid = true,
                                locationValidationMessage = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isFetchingLocationDetails = false,
                                finalCiudad = null,
                                finalComunidadAutonoma = null,
                                finalPostalCode = null,
                                isManualLocationValid = false,
                                locationValidationMessage = "Ubicación no encontrada para '$query'."
                            )
                        }
                    }
                }

                is Result.Error -> {
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

    fun prepareAndUpdateCar() {
        val state = _uiState.value
        val userId = firebaseAuth.currentUser?.uid
        val carId = state.carId

        if (!NetworkUtils.isInternetAvailable(applicationContext)) {
            _uiState.update { it.copy(generalErrorMessage = "No hay conexión a internet.") }
            return
        }

        if (userId == null || carId == null) {
            _uiState.update { it.copy(generalErrorMessage = "Error: Usuario no autenticado o ID de coche no encontrado.") }
            return
        }

        if (state.selectedBrand == null || state.selectedModel == null || state.selectedBodyType == null || state.selectedFuelType == null || state.selectedYear == null || state.selectedVersion == null || state.selectedCarColor == null || state.price.isBlank() || state.mileage.isBlank() || state.description.isBlank()) {
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
            val finalImageUrls = state.existingImageUrls.toMutableList()

            if (state.newSelectedImageUris.isNotEmpty()) {
                val uploadJobs = state.newSelectedImageUris.map { uri ->
                    async {
                        val imageName = "image_${System.currentTimeMillis()}.jpg"
                        val storagePath = "car_images/$carId/$imageName"
                        imageStorageDataSource.uploadImage(uri, storagePath)
                    }
                }

                val results = uploadJobs.awaitAll()
                val newUrls = mutableListOf<String>()
                var uploadFailed = false

                results.forEach { result ->
                    when (result) {
                        is Result.Success -> newUrls.add(result.data)
                        is Result.Error -> uploadFailed = true
                    }
                }

                if (uploadFailed) {
                    _uiState.update {
                        it.copy(
                            isUploadingImages = false,
                            formUploadInProgress = false,
                            imageUploadErrorMessage = "Error al subir una o más imágenes nuevas."
                        )
                    }
                    return@launch
                }
                finalImageUrls.addAll(newUrls)
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

            val carToUpdate = CarForSale(
                id = carId,
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
                imageUrls = finalImageUrls,
                comunidadAutonoma = state.finalComunidadAutonoma?.takeIf { it.isNotBlank() },
                ciudad = state.finalCiudad?.takeIf { it.isNotBlank() },
                postalCode = state.finalPostalCode?.takeIf { it.isNotBlank() },
                searchableKeywords = keywords
            )

            when (val updateResult = uploadRepository.updateCar(carToUpdate)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            formUploadInProgress = false,
                            formUploadSuccess = true,
                            generalErrorMessage = null
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            formUploadInProgress = false,
                            formUploadSuccess = false,
                            generalErrorMessage = "Error al actualizar los datos: ${updateResult.error.message}"
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
