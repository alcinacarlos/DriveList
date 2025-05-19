package com.carlosalcina.drivelist.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.domain.model.CarSearchFilters
import com.carlosalcina.drivelist.domain.model.QuickFilter
import com.carlosalcina.drivelist.domain.model.QuickFilterType
import com.carlosalcina.drivelist.domain.usecase.GetBrandsUseCase
import com.carlosalcina.drivelist.domain.usecase.GetModelsUseCase
import com.carlosalcina.drivelist.domain.usecase.GetUserFavoriteIdsUseCase
import com.carlosalcina.drivelist.domain.usecase.SearchCarsUseCase
import com.carlosalcina.drivelist.domain.usecase.ToggleFavoriteCarUseCase
import com.carlosalcina.drivelist.navigation.NavigationArgs
import com.carlosalcina.drivelist.ui.view.states.SearchVehicleScreenState
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SearchVehicleScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val searchCarsUseCase: SearchCarsUseCase,
    private val getBrandsUseCase: GetBrandsUseCase,
    private val getModelsUseCase: GetModelsUseCase,
    private val getUserFavoriteIdsUseCase: GetUserFavoriteIdsUseCase,
    private val toggleFavoriteCarUseCase: ToggleFavoriteCarUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchVehicleScreenState())
    val uiState: StateFlow<SearchVehicleScreenState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    companion object {
        private const val TAG = "SearchVehicleVM"
        private const val RESULTS_PER_PAGE = 10 // Límite para cada carga/paginación
        private const val SEARCH_DEBOUNCE_MS = 500L
    }

    init {
        observeAuthState()
        processInitialFiltersAndSearch()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            firebaseAuth.addAuthStateListener { auth ->
                val user = auth.currentUser
                val currentUserId = user?.uid
                val isAuthenticated = user != null
                val previousUserId = _uiState.value.currentUserId

                _uiState.update {
                    it.copy(
                        currentUserId = currentUserId,
                        isUserAuthenticated = isAuthenticated
                    )
                }
                if (isAuthenticated && currentUserId != null) {
                    if (currentUserId != previousUserId || _uiState.value.favoriteCarIds.isEmpty()) {
                        fetchUserFavorites(currentUserId)
                    }
                } else {
                    _uiState.update { currentState ->
                        currentState.copy(
                            favoriteCarIds = emptySet(),
                            // Actualizar los coches para que no aparezcan como favoritos
                            searchResults = currentState.searchResults.map { it.copy(isFavoriteByCurrentUser = false) }
                        )
                    }
                }
            }
        }
    }

    private fun processInitialFiltersAndSearch() {
        val initialFiltersJson: String? = savedStateHandle[NavigationArgs.SEARCH_FILTERS_JSON_ARG]
        var initialFilters = CarSearchFilters()
        if (initialFiltersJson != null) {
            try {
                initialFilters = Gson().fromJson(initialFiltersJson, CarSearchFilters::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializando filtros iniciales JSON", e)
            }
        }

        // Sincronizar activeQuickFilterIds con los filtros iniciales
        val newActiveQuickFilterIds = mutableSetOf<String>()
        _uiState.value.quickFilters.forEach { qf ->
            when (qf.type) {
                QuickFilterType.FUEL_TYPE -> if (initialFilters.fuelType == qf.value) newActiveQuickFilterIds.add(qf.id)
                QuickFilterType.MAX_PRICE -> if (initialFilters.maxPrice == qf.value) newActiveQuickFilterIds.add(qf.id)
                QuickFilterType.MIN_YEAR -> if (initialFilters.minYear == qf.value) newActiveQuickFilterIds.add(qf.id)
                QuickFilterType.LOCATION -> {
                    // Asumir que el valor del filtro rápido de ubicación se aplica a 'ciudad'
                    if (initialFilters.ciudad == qf.value || initialFilters.comunidadAutonoma == qf.value) newActiveQuickFilterIds.add(qf.id)
                }
            }
        }

        _uiState.update {
            it.copy(
                appliedFilters = initialFilters,
                currentSearchTerm = initialFilters.searchTerm ?: "",
                activeQuickFilterIds = newActiveQuickFilterIds,
                // Poblar campos del diálogo avanzado con los filtros iniciales
                tempAdvancedFilters = initialFilters,
                advancedFilterMinYearInput = initialFilters.minYear?.toString() ?: "",
                advancedFilterMaxPriceInput = initialFilters.maxPrice?.toInt()?.toString() ?: "", // Mostrar como Int si es posible
                advancedFilterLocationInput = initialFilters.ciudad ?: initialFilters.comunidadAutonoma ?: ""
            )
        }
        Log.d(TAG, "Filtros iniciales procesados: $initialFilters")
        if (initialFilters != CarSearchFilters() || !initialFilters.searchTerm.isNullOrBlank()) {
            performSearch(isNewSearch = true)
        }
    }


    private fun fetchUserFavorites(userId: String) {
        viewModelScope.launch {
            when (val result = getUserFavoriteIdsUseCase(userId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            favoriteCarIds = result.data.toSet(),
                            searchResults = it.searchResults.map { car -> car.copy(isFavoriteByCurrentUser = result.data.contains(car.id)) }
                        )
                    }
                }
                is Result.Error -> Log.e(TAG, "Error fetching user favorites: ${result.error.message}")
            }
        }
    }

    fun onSearchTermChanged(term: String) {
        _uiState.update { it.copy(currentSearchTerm = term) }
        searchJob?.cancel() // Cancelar búsqueda anterior con debounce
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS) // Esperar antes de buscar automáticamente
            onPerformSearchFromBar()
        }
    }

    fun onPerformSearchFromBar() { // Llamado por el botón de búsqueda o después del debounce
        searchJob?.cancel() // Cancelar cualquier job de debounce pendiente
        val currentTerm = _uiState.value.currentSearchTerm
        _uiState.update {
            it.copy(appliedFilters = it.appliedFilters.copy(searchTerm = currentTerm.ifBlank { null }))
        }
        performSearch(isNewSearch = true)
    }

    fun onApplyQuickFilter(quickFilter: QuickFilter) {
        var updatedFilters = _uiState.value.appliedFilters
        val newActiveQuickFilterIds = _uiState.value.activeQuickFilterIds.toMutableSet()

        val alreadyActive = newActiveQuickFilterIds.contains(quickFilter.id)

        // Lógica para quitar filtros del mismo tipo si son mutuamente excluyentes
        _uiState.value.quickFilters
            .filter { it.type == quickFilter.type && it.id != quickFilter.id && newActiveQuickFilterIds.contains(it.id) }
            .forEach { newActiveQuickFilterIds.remove(it.id) }

        if (alreadyActive) {
            newActiveQuickFilterIds.remove(quickFilter.id)
            updatedFilters = when (quickFilter.type) {
                QuickFilterType.FUEL_TYPE -> updatedFilters.copy(fuelType = null)
                QuickFilterType.MAX_PRICE -> updatedFilters.copy(maxPrice = null)
                QuickFilterType.MIN_YEAR -> updatedFilters.copy(minYear = null)
                QuickFilterType.LOCATION -> updatedFilters.copy(ciudad = null, comunidadAutonoma = null) // Limpia ambos
            }
        } else {
            newActiveQuickFilterIds.add(quickFilter.id)
            updatedFilters = when (quickFilter.type) {
                QuickFilterType.FUEL_TYPE -> updatedFilters.copy(fuelType = quickFilter.value as? String)
                QuickFilterType.MAX_PRICE -> updatedFilters.copy(maxPrice = quickFilter.value as? Double)
                QuickFilterType.MIN_YEAR -> updatedFilters.copy(minYear = quickFilter.value as? Int)
                QuickFilterType.LOCATION -> {
                    // Asumir que el valor del filtro rápido de ubicación se aplica a 'ciudad'
                    // Podrías tener una lógica más compleja si quieres diferenciar
                    updatedFilters.copy(ciudad = quickFilter.value as? String, comunidadAutonoma = null)
                }
            }
        }

        _uiState.update { it.copy(appliedFilters = updatedFilters, activeQuickFilterIds = newActiveQuickFilterIds) }
        performSearch(isNewSearch = true)
    }


    private fun performSearch(isNewSearch: Boolean) {
        val currentUserId = _uiState.value.currentUserId
        // Usar los filtros aplicados, que ya incluyen el searchTerm de la barra si se pulsó buscar
        val filtersToUse = _uiState.value.appliedFilters

        Log.d(TAG, "Performing search with filters: $filtersToUse, isNewSearch: $isNewSearch, page: ${if(isNewSearch) 0 else _uiState.value.currentPage +1}")

        viewModelScope.launch {
            if (isNewSearch) {
                _uiState.update { it.copy(isLoading = true, searchError = null, noResultsFound = false, searchResults = emptyList(), canLoadMore = true, currentPage = 0) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true, searchError = null) }
            }

            // Aquí se podría añadir lógica para paginación si 'searchCarsUseCase' la soporta
            // (ej. pasando el último documento visible para 'startAfter' o un offset)
            when (val result = searchCarsUseCase(filters = filtersToUse, limit = RESULTS_PER_PAGE, currentUserId = currentUserId)) {
                is Result.Success -> {
                    val newCars = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            searchResults = if (isNewSearch) newCars else it.searchResults + newCars,
                            noResultsFound = if (isNewSearch) newCars.isEmpty() else (it.searchResults + newCars).isEmpty(),
                            canLoadMore = newCars.size == RESULTS_PER_PAGE,
                            currentPage = if (isNewSearch) 0 else it.currentPage + 1
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            searchError = result.error.message ?: "Error desconocido en la búsqueda"
                        )
                    }
                }
            }
        }
    }

    fun onLoadMoreResults() {
        if (_uiState.value.canLoadMore && !_uiState.value.isLoadingMore && !_uiState.value.isLoading) {
            Log.d(TAG, "Cargando más resultados para la página: ${_uiState.value.currentPage + 1}")
            performSearch(isNewSearch = false)
        }
    }

    // --- Lógica para Filtros Avanzados (Marca/Modelo/Año/Ubicación) ---
    fun openAdvancedFiltersDialog() {
        // Cargar marcas si no están ya cargadas para el diálogo
        if (_uiState.value.brandsForDialog.isEmpty() && !_uiState.value.isLoadingBrandsForDialog) {
            fetchBrandsForDialog()
        }
        // Inicializar tempAdvancedFilters con los filtros aplicados actualmente
        _uiState.update {
            it.copy(
                showAdvancedFiltersDialog = true,
                tempAdvancedFilters = it.appliedFilters, // Copia los filtros actuales al diálogo
                selectedBrandInDialog = it.appliedFilters.brand, // Pre-seleccionar marca si existe
                selectedModelInDialog = it.appliedFilters.model, // Pre-seleccionar modelo
                advancedFilterMinYearInput = it.appliedFilters.minYear?.toString() ?: "",
                advancedFilterMaxPriceInput = it.appliedFilters.maxPrice?.toInt()?.toString() ?: "",
                advancedFilterLocationInput = it.appliedFilters.ciudad ?: it.appliedFilters.comunidadAutonoma ?: ""
            )
        }
        // Si hay una marca pre-seleccionada, cargar sus modelos
        _uiState.value.appliedFilters.brand?.let { currentBrand ->
            if (_uiState.value.brandsForDialog.contains(currentBrand) || _uiState.value.brandsForDialog.isEmpty()){ // Si las marcas están cargadas o se van a cargar
                onBrandSelectedInDialog(currentBrand, preselectModel = _uiState.value.appliedFilters.model)
            }
        }
    }

    fun closeAdvancedFiltersDialog() {
        _uiState.update { it.copy(showAdvancedFiltersDialog = false) }
    }

    private fun fetchBrandsForDialog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBrandsForDialog = true, brandLoadErrorForDialog = null) }
            when (val result = getBrandsUseCase()) {
                is Result.Success -> _uiState.update { it.copy(isLoadingBrandsForDialog = false, brandsForDialog = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoadingBrandsForDialog = false, brandLoadErrorForDialog = result.error.message) }
            }
        }
    }

    fun onBrandSelectedInDialog(brand: String, preselectModel: String? = null) {
        _uiState.update {
            it.copy(
                selectedBrandInDialog = brand,
                selectedModelInDialog = null, // Reset model when brand changes
                isLoadingModelsForDialog = true,
                modelLoadErrorForDialog = null,
                modelsForDialog = emptyList(),
                tempAdvancedFilters = it.tempAdvancedFilters.copy(brand = brand, model = null) // Actualizar temp
            )
        }
        viewModelScope.launch {
            when (val result = getModelsUseCase(brand)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingModelsForDialog = false,
                            modelsForDialog = result.data,
                            // Si hay un modelo para preseleccionar y existe en la lista, seleccionarlo
                            selectedModelInDialog = if (preselectModel != null && result.data.contains(preselectModel)) preselectModel else null,
                            tempAdvancedFilters = it.tempAdvancedFilters.copy(model = if (preselectModel != null && result.data.contains(preselectModel)) preselectModel else null)
                        )
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isLoadingModelsForDialog = false, modelLoadErrorForDialog = result.error.message) }
            }
        }
    }

    fun onModelSelectedInDialog(model: String) {
        _uiState.update {
            it.copy(
                selectedModelInDialog = model,
                tempAdvancedFilters = it.tempAdvancedFilters.copy(model = model)
            )
        }
    }

    fun onAdvancedFilterMinYearChanged(year: String) {
        _uiState.update { it.copy(advancedFilterMinYearInput = year, tempAdvancedFilters = it.tempAdvancedFilters.copy(minYear = year.toIntOrNull())) }
    }
    fun onAdvancedFilterMaxPriceChanged(price: String) {
        _uiState.update { it.copy(advancedFilterMaxPriceInput = price, tempAdvancedFilters = it.tempAdvancedFilters.copy(maxPrice = price.toDoubleOrNull())) }
    }
    fun onAdvancedFilterLocationChanged(location: String) {
        // Asumimos que la entrada de ubicación en el diálogo se aplica a 'ciudad'
        // Podrías tener dos campos si quieres ser más específico (ciudad, comunidad)
        _uiState.update { it.copy(advancedFilterLocationInput = location, tempAdvancedFilters = it.tempAdvancedFilters.copy(ciudad = location.ifBlank { null }, comunidadAutonoma = null)) }
    }

    fun applyAdvancedFilters() {
        val tempFilters = _uiState.value.tempAdvancedFilters
        _uiState.update {
            it.copy(
                appliedFilters = tempFilters,
                showAdvancedFiltersDialog = false,
                // Sincronizar activeQuickFilterIds con los nuevos filtros avanzados
                activeQuickFilterIds = syncQuickFiltersWithApplied(tempFilters, it.quickFilters)
            )
        }
        performSearch(isNewSearch = true)
    }

    fun clearAdvancedFilters() { // Limpia los filtros DENTRO del diálogo
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        _uiState.update {
            it.copy(
                tempAdvancedFilters = CarSearchFilters(searchTerm = it.appliedFilters.searchTerm), // Mantener searchTerm
                selectedBrandInDialog = null,
                selectedModelInDialog = null,
                modelsForDialog = emptyList(),
                advancedFilterMinYearInput = "",
                advancedFilterMaxPriceInput = "",
                advancedFilterLocationInput = ""
            )
        }
    }

    // Helper para sincronizar QuickFilters después de aplicar filtros avanzados
    private fun syncQuickFiltersWithApplied(applied: CarSearchFilters, quickFiltersList: List<QuickFilter>): Set<String> {
        val newActiveIds = mutableSetOf<String>()
        quickFiltersList.forEach { qf ->
            val isActive = when (qf.type) {
                QuickFilterType.FUEL_TYPE -> applied.fuelType == qf.value
                QuickFilterType.MAX_PRICE -> applied.maxPrice == qf.value
                QuickFilterType.MIN_YEAR -> applied.minYear == qf.value
                QuickFilterType.LOCATION -> applied.ciudad == qf.value || applied.comunidadAutonoma == qf.value
            }
            if (isActive) newActiveIds.add(qf.id)
        }
        return newActiveIds
    }


    // --- Lógica de Favoritos (sin cambios significativos, solo actualiza searchResults) ---
    fun toggleFavoriteStatus(carId: String) {
        val currentUserId = _uiState.value.currentUserId
        if (currentUserId == null) {
            _uiState.update { it.copy(favoriteToggleError = "Debes iniciar sesión para añadir favoritos.") }
            return
        }
        val isCurrentlyFavorite = _uiState.value.favoriteCarIds.contains(carId)

        _uiState.update {
            it.copy(
                isTogglingFavorite = it.isTogglingFavorite + (carId to true),
                favoriteToggleError = null
            )
        }

        viewModelScope.launch {
            when (val result = toggleFavoriteCarUseCase(userId = currentUserId, carId = carId, isCurrentlyFavorite = isCurrentlyFavorite)) {
                is Result.Success -> {
                    val updatedFavoriteIds = if (isCurrentlyFavorite) {
                        _uiState.value.favoriteCarIds - carId
                    } else {
                        _uiState.value.favoriteCarIds + carId
                    }
                    _uiState.update {
                        it.copy(
                            isTogglingFavorite = it.isTogglingFavorite - carId,
                            favoriteCarIds = updatedFavoriteIds,
                            searchResults = it.searchResults.map { car ->
                                if (car.id == carId) car.copy(isFavoriteByCurrentUser = !isCurrentlyFavorite) else car
                            }
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isTogglingFavorite = it.isTogglingFavorite - carId,
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
