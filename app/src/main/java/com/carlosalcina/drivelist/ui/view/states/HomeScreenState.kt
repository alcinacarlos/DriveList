package com.carlosalcina.drivelist.ui.view.states

import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.CarSearchFilters

data class HomeScreenState(
    // Estado de los coches
    val isLoadingLatestCars: Boolean = false,
    val latestCars: List<CarForSale> = emptyList(),
    val carLoadError: String? = null,

    val isLoadingSearchedCars: Boolean = false, // Aunque la búsqueda principal se mueva a otra pantalla
    val searchedCars: List<CarForSale> = emptyList(),
    val searchError: String? = null,
    val noSearchResults: Boolean = false,

    // Filtros de búsqueda
    val filters: CarSearchFilters = CarSearchFilters(),
    val showBrandModelDialog: Boolean = false,

    // Marcas y Modelos para el diálogo de filtro
    val isLoadingBrands: Boolean = false,
    val brands: List<String> = emptyList(),
    val brandLoadError: String? = null,

    val isLoadingModels: Boolean = false,
    val models: List<String> = emptyList(),
    val modelLoadError: String? = null,
    val selectedBrandForDialog: String? = null,

    // Favoritos
    val favoriteCarIds: Set<String> = emptySet(),
    // CORRECCIÓN AQUÍ: isTogglingFavorite debe ser un Map para la lógica implementada
    val isTogglingFavorite: Map<String, Boolean> = emptyMap(), // carId -> isLoading
    val favoriteToggleError: String? = null,

    // Autenticación
    val currentUserId: String? = null,
    val isUserAuthenticated: Boolean = false
)
