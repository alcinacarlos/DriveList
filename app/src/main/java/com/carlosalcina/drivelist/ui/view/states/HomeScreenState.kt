package com.carlosalcina.drivelist.ui.view.states

import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.CarSearchFilters

data class HomeScreenState(
    // Estado de los coches
    val isLoadingLatestCars: Boolean = false,
    val latestCars: List<CarForSale> = emptyList(),
    val carLoadError: String? = null,

    val isLoadingSearchedCars: Boolean = false,
    val searchedCars: List<CarForSale> = emptyList(), // Para los resultados de búsqueda
    val searchError: String? = null,
    val noSearchResults: Boolean = false, // Para indicar si la búsqueda no arrojó resultados

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
    val selectedBrandForDialog: String? = null, // Marca seleccionada dentro del diálogo

    // Favoritos
    val favoriteCarIds: Set<String> = emptySet(), // IDs de los coches favoritos del usuario
    val isTogglingFavorite: Boolean = false, // Para mostrar un indicador de carga al marcar favorito
    val favoriteToggleError: String? = null,

    // Autenticación
    val currentUserId: String? = null,
    val isUserAuthenticated: Boolean = false
)
