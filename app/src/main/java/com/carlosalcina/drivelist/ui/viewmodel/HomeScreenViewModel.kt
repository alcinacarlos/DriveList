package com.carlosalcina.drivelist.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.domain.usecase.GetBrandsUseCase
import com.carlosalcina.drivelist.domain.usecase.GetLatestCarsUseCase
import com.carlosalcina.drivelist.domain.usecase.GetModelsUseCase
import com.carlosalcina.drivelist.domain.usecase.GetUserFavoriteIdsUseCase
import com.carlosalcina.drivelist.domain.usecase.SearchCarsUseCase
import com.carlosalcina.drivelist.domain.usecase.ToggleFavoriteCarUseCase
import com.carlosalcina.drivelist.ui.view.states.HomeScreenState
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val getLatestCarsUseCase: GetLatestCarsUseCase,
    private val searchCarsUseCase: SearchCarsUseCase,
    private val getBrandsUseCase: GetBrandsUseCase,
    private val getModelsUseCase: GetModelsUseCase,
    private val getUserFavoriteIdsUseCase: GetUserFavoriteIdsUseCase,
    private val toggleFavoriteCarUseCase: ToggleFavoriteCarUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeScreenState()
    )

    // Lista predefinida de tipos de combustible para el filtro (podría venir de recursos o dominio)
    val fuelTypesForFilter: List<String> = listOf("Gasolina", "Diésel", "Híbrido", "Eléctrico", "GLP", "CNG")

    init {
        observeAuthState()
        // Las cargas iniciales (marcas, coches recientes, favoritos) se dispararán cuando el usuario esté autenticado.
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            firebaseAuth.addAuthStateListener { auth ->
                val user = auth.currentUser
                val currentUserId = user?.uid
                val isAuthenticated = user != null
                _uiState.update {
                    it.copy(
                        currentUserId = currentUserId,
                        isUserAuthenticated = isAuthenticated
                    )
                }
                if (isAuthenticated && currentUserId != null) {
                    // Cargar datos iniciales solo si el usuario está autenticado y no se han cargado ya
                    if (_uiState.value.latestCars.isEmpty() && !_uiState.value.isLoadingLatestCars) {
                        fetchLatestCars()
                    }
                    if (_uiState.value.brands.isEmpty() && !_uiState.value.isLoadingBrands) {
                        fetchBrands()
                    }
                    if (_uiState.value.favoriteCarIds.isEmpty()) { // Cargar favoritos
                        fetchUserFavorites(currentUserId)
                    }
                } else {
                    // Limpiar datos sensibles si el usuario se desloguea
                    _uiState.update {
                        it.copy(
                            latestCars = emptyList(),
                            searchedCars = emptyList(),
                            favoriteCarIds = emptySet()
                            // No limpiar filtros necesariamente
                        )
                    }
                }
            }
        }
    }

    private fun fetchUserFavorites(userId: String) {
        viewModelScope.launch {
            // No necesitamos un isLoading para esto, ya que se actualiza en segundo plano
            when (val result = getUserFavoriteIdsUseCase(userId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            favoriteCarIds = result.data.toSet(),
                            // Actualizar el estado de favorito de los coches ya cargados
                            latestCars = it.latestCars.map { car -> car.copy(isFavoriteByCurrentUser = result.data.contains(car.id)) },
                            searchedCars = it.searchedCars.map { car -> car.copy(isFavoriteByCurrentUser = result.data.contains(car.id)) }
                        )
                    }
                }
                is Result.Error -> {
                    Log.e("HomeScreenVM", "Error fetching user favorites: ${result.error.message}")
                    // Podrías mostrar un error sutil si es necesario
                }
            }
        }
    }

    fun fetchLatestCars() {
        val currentUserId = _uiState.value.currentUserId
        // No es necesario verificar autenticación aquí si se llama desde observeAuthState
        // o si el caso de uso maneja currentUserId nulo (como lo hace ahora)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLatestCars = true, carLoadError = null) }
            when (val result = getLatestCarsUseCase(limit = 20, currentUserId = currentUserId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingLatestCars = false,
                            latestCars = result.data // El UseCase ya debería devolver coches con isFavoriteByCurrentUser poblado
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoadingLatestCars = false, carLoadError = result.error.message ?: "Error desconocido") }
                }
            }
        }
    }

    fun fetchBrands() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBrands = true, brandLoadError = null) }
            when (val result = getBrandsUseCase()) { // No necesita userId
                is Result.Success -> {
                    _uiState.update { it.copy(isLoadingBrands = false, brands = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoadingBrands = false, brandLoadError = result.error.message ?: "Error desconocido") }
                }
            }
        }
    }

    fun onBrandSelectedInDialog(brand: String) {
        _uiState.update { it.copy(selectedBrandForDialog = brand, isLoadingModels = true, modelLoadError = null, models = emptyList()) }
        viewModelScope.launch {
            when (val result = getModelsUseCase(brand)) { // No necesita userId
                is Result.Success -> {
                    _uiState.update { it.copy(isLoadingModels = false, models = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoadingModels = false, modelLoadError = result.error.message ?: "Error desconocido") }
                }
            }
        }
    }

    fun onModelSelectedInDialog(model: String) {
        val currentBrand = _uiState.value.selectedBrandForDialog
        if (currentBrand != null) {
            _uiState.update {
                it.copy(
                    filters = it.filters.copy(brand = currentBrand, model = model),
                    showBrandModelDialog = false,
                    selectedBrandForDialog = null,
                    models = emptyList()
                )
            }
        }
    }

    fun openBrandModelDialog() {
        val currentBrandFilter = _uiState.value.filters.brand
        _uiState.update { it.copy(showBrandModelDialog = true, selectedBrandForDialog = currentBrandFilter) }
        if (_uiState.value.brands.isEmpty() && !_uiState.value.isLoadingBrands) {
            fetchBrands()
        } else if (currentBrandFilter != null && _uiState.value.brands.contains(currentBrandFilter)) {
            onBrandSelectedInDialog(currentBrandFilter) // Cargar modelos si la marca ya está seleccionada
        }
    }

    fun closeBrandModelDialog() {
        _uiState.update { it.copy(showBrandModelDialog = false, selectedBrandForDialog = null, models = emptyList()) }
    }

    fun onMaxPriceChanged(price: String) {
        val priceDouble = price.filter { it.isDigit() }.toDoubleOrNull() // Limpiar y convertir
        _uiState.update { it.copy(filters = it.filters.copy(maxPrice = priceDouble)) }
    }

    fun onFuelTypeSelected(fuelType: String?) {
        _uiState.update { it.copy(filters = it.filters.copy(fuelType = fuelType)) }
    }

    fun performSearch() {
        val currentUserId = _uiState.value.currentUserId
        val currentFilters = _uiState.value.filters
        Log.d("HomeScreenVM", "Buscando con filtros: $currentFilters")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSearchedCars = true, searchError = null, noSearchResults = false) }
            when (val result = searchCarsUseCase(filters = currentFilters, limit = 20, currentUserId = currentUserId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingSearchedCars = false,
                            searchedCars = result.data,
                            noSearchResults = result.data.isEmpty()
                            // Podrías querer reemplazar latestCars con searchedCars o tener una sección separada
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoadingSearchedCars = false, searchError = result.error.message ?: "Error desconocido") }
                }
            }
        }
    }

    fun clearBrandModelFilter() {
        _uiState.update {
            it.copy(
                filters = it.filters.copy(brand = null, model = null),
                selectedBrandForDialog = null,
                models = emptyList()
            )
        }
    }

    fun toggleFavoriteStatus(carId: String) {
        val currentUserId = _uiState.value.currentUserId
        if (currentUserId == null) {
            _uiState.update { it.copy(favoriteToggleError = "Debes iniciar sesión para añadir favoritos.") }
            return
        }

        val isCurrentlyFavorite = _uiState.value.favoriteCarIds.contains(carId)
        _uiState.update { it.copy(isTogglingFavorite = true, favoriteToggleError = null) }

        viewModelScope.launch {
            when (val result = toggleFavoriteCarUseCase(userId = currentUserId, carId = carId, isCurrentlyFavorite = isCurrentlyFavorite)) {
                is Result.Success -> {
                    // Actualizar la lista local de favoritos y el estado de los coches
                    val updatedFavoriteIds = if (isCurrentlyFavorite) {
                        _uiState.value.favoriteCarIds - carId
                    } else {
                        _uiState.value.favoriteCarIds + carId
                    }
                    _uiState.update {
                        it.copy(
                            isTogglingFavorite = false,
                            favoriteCarIds = updatedFavoriteIds,
                            latestCars = it.latestCars.map { car ->
                                if (car.id == carId) car.copy(isFavoriteByCurrentUser = !isCurrentlyFavorite) else car
                            },
                            searchedCars = it.searchedCars.map { car ->
                                if (car.id == carId) car.copy(isFavoriteByCurrentUser = !isCurrentlyFavorite) else car
                            }
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isTogglingFavorite = false,
                            favoriteToggleError = result.error.message ?: "Error al actualizar favorito."
                        )
                    }
                }
            }
        }
    }

    fun clearFavoriteToggleError() {
        _uiState.update { it.copy(favoriteToggleError = null) }
    }
}