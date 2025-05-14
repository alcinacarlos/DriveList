package com.carlosalcina.drivelist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.usecase.*
import com.carlosalcina.drivelist.ui.view.states.UploadCarScreenState
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadCarViewModel @Inject constructor(
    private val getBrandsUseCase: GetBrandsUseCase,
    private val getModelsUseCase: GetModelsUseCase,
    private val getBodyTypesUseCase: GetBodyTypesUseCase,
    private val getFuelTypesUseCase: GetFuelTypesUseCase,
    private val getYearsUseCase: GetYearsUseCase,
    private val getVersionsUseCase: GetVersionsUseCase,
    private val uploadCarDataUseCase: UploadCarDataUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadCarScreenState())
    val uiState: StateFlow<UploadCarScreenState> = _uiState.asStateFlow()

    init {
        loadBrands()
    }

    fun loadBrands() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBrands = true, generalErrorMessage = null) }
            when (val result = getBrandsUseCase()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoadingBrands = false, brands = result.data)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoadingBrands = false, generalErrorMessage = "Error cargando marcas: ${result.error.message}")
                    }
                }
            }
        }
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
            when (val result = getModelsUseCase(brand)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingModels = false, models = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoadingModels = false, generalErrorMessage = "Error cargando modelos: ${result.error.message}")
                }
            }
        }
    }

    fun onModelSelected(model: String) {
        val currentBrand = _uiState.value.selectedBrand ?: return // No debería pasar si la UI está bien
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
            when (val result = getBodyTypesUseCase(currentBrand, model)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingBodyTypes = false, bodyTypes = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoadingBodyTypes = false, generalErrorMessage = "Error cargando carrocerías: ${result.error.message}")
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
            when (val result = getFuelTypesUseCase(currentBrand, currentModel, bodyType)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingFuelTypes = false, fuelTypes = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoadingFuelTypes = false, generalErrorMessage = "Error cargando combustibles: ${result.error.message}")
                }
            }
        }
    }

    fun onFuelTypeSelected(fuelType: String) {
        val state = _uiState.value
        val (currentBrand, currentModel, currentBodyType) = Triple(state.selectedBrand, state.selectedModel, state.selectedBodyType)
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
            when (val result = getYearsUseCase(currentBrand, currentModel, currentBodyType, fuelType)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingYears = false, years = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoadingYears = false, generalErrorMessage = "Error cargando años: ${result.error.message}")
                }
            }
        }
    }

    fun onYearSelected(year: String) {
        val state = _uiState.value
        val (currentBrand, currentModel, currentBodyType, currentFuelType) = Quadruple(state.selectedBrand, state.selectedModel, state.selectedBodyType, state.selectedFuelType)
        if (currentBrand == null || currentModel == null || currentBodyType == null || currentFuelType == null) return

        _uiState.update {
            it.copy(
                selectedYear = year,
                versions = emptyList(), selectedVersion = null, // Resetear cascada
                isLoadingVersions = true, generalErrorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result = getVersionsUseCase(currentBrand, currentModel, currentBodyType, currentFuelType, year)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingVersions = false, versions = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoadingVersions = false, generalErrorMessage = "Error cargando versiones: ${result.error.message}")
                }
            }
        }
    }
    // Helper data class para el ViewModel
    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)


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

    fun submitCar() {
        val state = _uiState.value
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            _uiState.update { it.copy(generalErrorMessage = "Error: Usuario no autenticado.") }
            return
        }

        // Validaciones básicas
        if (state.selectedBrand == null || state.selectedModel == null ||
            state.selectedBodyType == null || state.selectedFuelType == null ||
            state.selectedYear == null || state.selectedVersion == null ||
            state.price.isBlank() || state.mileage.isBlank() || state.description.isBlank()) {
            _uiState.update { it.copy(generalErrorMessage = "Por favor, completa todos los campos.") }
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

        val carToUpload = CarForSale(
            // id se genera automáticamente en el constructor de CarForSale
            userId = userId,
            brand = state.selectedBrand,
            model = state.selectedModel,
            bodyType = state.selectedBodyType,
            fuelType = state.selectedFuelType,
            year = state.selectedYear,
            version = state.selectedVersion,
            price = priceDouble,
            mileage = mileageInt,
            description = state.description.trim()
            // imageUrls = ... // Esto requerirá un manejo aparte para la subida de imágenes
        )

        viewModelScope.launch {
            _uiState.update { it.copy(uploadInProgress = true, uploadSuccess = false, generalErrorMessage = null) }
            when (val result = uploadCarDataUseCase(carToUpload)) {
                is Result.Success -> {
                    _uiState.update { it.copy(uploadInProgress = false, uploadSuccess = true) }
                    // Aquí podrías resetear el formulario o navegar a otra pantalla
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(uploadInProgress = false, uploadSuccess = false, generalErrorMessage = "Error al subir el coche: ${result.error.message}")
                    }
                }
            }
        }
    }

    fun resetUploadStatus() {
        _uiState.update { it.copy(uploadSuccess = false, generalErrorMessage = null, uploadInProgress = false) }
    }
}