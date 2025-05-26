package com.carlosalcina.drivelist.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.domain.repository.UserFavoriteRepository
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
    private val uploadRepository: CarUploadRepository,
    private val userFavoriteRepository: UserFavoriteRepository,
    private val toggleFavoriteCarUseCase: ToggleFavoriteCarUseCase,
    private val carListRepository: CarListRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenState()) // Usa el HomeScreenState corregido
    val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeScreenState()
    )

    val fuelTypesForFilter: List<String> = listOf("Gasolina", "Diésel", "Híbrido", "Eléctrico", "GLP", "GNC")

    companion object {
        private const val TAG = "HomeScreenVM_Debug"
    }

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            val user = firebaseAuth.currentUser
            val currentUserId = user?.uid
            val isAuthenticated = user != null

            if (isAuthenticated && currentUserId != null) {
                if (_uiState.value.latestCars.isEmpty() || (_uiState.value.latestCars.isEmpty() && !_uiState.value.isLoadingLatestCars)) {
                        Log.d(TAG, "Conditions met to fetch latest cars and favorites.")
                        fetchLatestCars()
                        fetchUserFavorites(currentUserId)
                    }
                    if (_uiState.value.brands.isEmpty() && !_uiState.value.isLoadingBrands) {
                        fetchBrands()
                    }
            } else {
                Log.d(TAG, "User is not authenticated or UID is null. Clearing user-specific data.")
                _uiState.update {
                    it.copy(
                        latestCars = emptyList(),
                        searchedCars = emptyList(),
                        favoriteCarIds = emptySet(),
                        isTogglingFavorite = emptyMap(), // Limpiar también este mapa
                        carLoadError = null,
                        searchError = null
                    )
                }
            }
        }
    }

    private fun fetchUserFavorites(userId: String) {
        viewModelScope.launch {
            when (val result = userFavoriteRepository.getUserFavoriteCarIds(userId)) {
                is Result.Success -> {
                    Log.d(TAG, "Successfully fetched favorite IDs: ${result.data.size} items.")
                    _uiState.update {
                        it.copy(
                            favoriteCarIds = result.data.toSet(),
                            latestCars = it.latestCars.map { car -> car.copy(isFavoriteByCurrentUser = result.data.contains(car.id)) },
                            searchedCars = it.searchedCars.map { car -> car.copy(isFavoriteByCurrentUser = result.data.contains(car.id)) }
                        )
                    }
                }
                is Result.Error -> {
                    Log.e(TAG, "Error fetching user favorites: ${result.error.message}")
                }
            }
        }
    }

    fun fetchLatestCars() {
        val currentUserId = firebaseAuth.currentUser?.uid

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLatestCars = true, carLoadError = null) }
            when (val result = carListRepository.getLatestCars(limit = 20, currentUserId = currentUserId)) {
                is Result.Success -> {
                    if (result.data.isEmpty()) {
                        Log.w(TAG, "fetchLatestCars returned an empty list.")
                    }
                    result.data.take(3).forEach { Log.d(TAG, "Car sample: ${it.brand} ${it.model}, Fav: ${it.isFavoriteByCurrentUser}") }

                    _uiState.update {
                        it.copy(
                            isLoadingLatestCars = false,
                            latestCars = result.data
                        )
                    }
                }
                is Result.Error -> {
                    Log.e(TAG, "fetchLatestCars error: ${result.error.message}")
                    _uiState.update { it.copy(isLoadingLatestCars = false, carLoadError = result.error.message ?: "Error desconocido al cargar coches") }
                }
            }
        }
    }

    fun fetchBrands() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBrands = true, brandLoadError = null) }
            when (val result = uploadRepository.getBrands()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoadingBrands = false, brands = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoadingBrands = false, brandLoadError = result.error.message) }
                }
            }
        }
    }

    fun onBrandSelectedInDialog(brand: String) {
        _uiState.update { it.copy(selectedBrandForDialog = brand, isLoadingModels = true, modelLoadError = null, models = emptyList()) }
        viewModelScope.launch {
            when (val result = uploadRepository.getModels(brand)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoadingModels = false, models = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoadingModels = false, modelLoadError = result.error.message) }
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
            onBrandSelectedInDialog(currentBrandFilter)
        }
    }

    fun closeBrandModelDialog() {
        _uiState.update { it.copy(showBrandModelDialog = false, selectedBrandForDialog = null, models = emptyList()) }
    }

    fun onMaxPriceChanged(price: String) {
        val priceDouble = price.filter { it.isDigit() }.toDoubleOrNull()
        _uiState.update { it.copy(filters = it.filters.copy(maxPrice = priceDouble)) }
    }

    fun onFuelTypeSelected(fuelType: String?) {
        _uiState.update { it.copy(filters = it.filters.copy(fuelType = fuelType)) }
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
        _uiState.update { currentState ->
            // Crear una nueva instancia del mapa, añadiendo o actualizando la entrada para carId
            val newTogglingFavoriteMap = currentState.isTogglingFavorite.toMutableMap()
            newTogglingFavoriteMap[carId] = true
            currentState.copy(isTogglingFavorite = newTogglingFavoriteMap, favoriteToggleError = null)
        }

        viewModelScope.launch {
            when (val result = toggleFavoriteCarUseCase(userId = currentUserId, carId = carId, isCurrentlyFavorite = isCurrentlyFavorite)) {
                is Result.Success -> {
                    val updatedFavoriteIds = if (isCurrentlyFavorite) {
                        _uiState.value.favoriteCarIds - carId
                    } else {
                        _uiState.value.favoriteCarIds + carId
                    }
                    _uiState.update { currentState ->
                        // Crear una nueva instancia del mapa, eliminando la entrada para carId
                        val newTogglingFavoriteMap = currentState.isTogglingFavorite.toMutableMap()
                        newTogglingFavoriteMap.remove(carId)
                        currentState.copy(
                            isTogglingFavorite = newTogglingFavoriteMap,
                            favoriteCarIds = updatedFavoriteIds,
                            latestCars = currentState.latestCars.map { car ->
                                if (car.id == carId) car.copy(isFavoriteByCurrentUser = !isCurrentlyFavorite) else car
                            },
                            searchedCars = currentState.searchedCars.map { car ->
                                if (car.id == carId) car.copy(isFavoriteByCurrentUser = !isCurrentlyFavorite) else car
                            }
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { currentState ->
                        // Crear una nueva instancia del mapa, eliminando la entrada para carId
                        val newTogglingFavoriteMap = currentState.isTogglingFavorite.toMutableMap()
                        newTogglingFavoriteMap.remove(carId)
                        currentState.copy(
                            isTogglingFavorite = newTogglingFavoriteMap,
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
