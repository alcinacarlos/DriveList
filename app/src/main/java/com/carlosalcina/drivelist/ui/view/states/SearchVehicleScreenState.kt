package com.carlosalcina.drivelist.ui.view.states

import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.CarSearchFilters
import com.carlosalcina.drivelist.domain.model.QuickFilter
import com.carlosalcina.drivelist.domain.model.QuickFilterType

data class SearchVehicleScreenState(
    // Búsqueda y Filtros
    val currentSearchTerm: String = "", // Término en la barra de búsqueda de esta pantalla
    var appliedFilters: CarSearchFilters = CarSearchFilters(), // Filtros actualmente aplicados

    val quickFilters: List<QuickFilter> = listOf(
        QuickFilter("qf_gasolina", "Gasolina", QuickFilterType.FUEL_TYPE, "Gasolina"),
        QuickFilter("qf_diesel", "Diésel", QuickFilterType.FUEL_TYPE, "Diésel"),
        QuickFilter("qf_hibrido", "Híbrido", QuickFilterType.FUEL_TYPE, "Híbrido"),
        QuickFilter("qf_electrico", "Eléctrico", QuickFilterType.FUEL_TYPE, "Eléctrico"),
        QuickFilter("qf_precio_5k", "< 5.000€", QuickFilterType.MAX_PRICE, 5000.0),
        QuickFilter("qf_precio_10k", "< 10.000€", QuickFilterType.MAX_PRICE, 10000.0),
        QuickFilter("qf_min_year_2020", "Desde 2020", QuickFilterType.MIN_YEAR, 2020),
        QuickFilter("qf_min_year_2018", "Desde 2018", QuickFilterType.MIN_YEAR, 2018),
        // Para LOCATION, el 'value' será la ciudad/comunidad y el ViewModel decidirá dónde aplicarlo
        // O podrías tener un 'fieldToUpdate' en QuickFilter si quieres ser más explícito
        QuickFilter("qf_loc_madrid", "Madrid", QuickFilterType.LOCATION, "Madrid"),
        QuickFilter("qf_loc_barcelona", "Barcelona", QuickFilterType.LOCATION, "Barcelona")
    ),
    val activeQuickFilterIds: Set<String> = emptySet(),

    // Resultados
    val searchResults: List<CarForSale> = emptyList(),
    val isLoading: Boolean = false,
    val searchError: String? = null,
    val noResultsFound: Boolean = false,

    // Paginación
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val currentPage: Int = 0, // Para la paginación basada en offset o para saber cuándo es la primera carga

    // Favoritos
    val favoriteCarIds: Set<String> = emptySet(),
    val isTogglingFavorite: Map<String, Boolean> = emptyMap(), // carId -> isLoading
    val favoriteToggleError: String? = null,

    // Autenticación
    val currentUserId: String? = null,
    val isUserAuthenticated: Boolean = false,

    // Para el diálogo de filtros avanzados (marca/modelo/año/ubicación)
    val showAdvancedFiltersDialog: Boolean = false,
    val tempAdvancedFilters: CarSearchFilters = CarSearchFilters(), // Filtros temporales para el diálogo

    val isLoadingBrandsForDialog: Boolean = false,
    val brandsForDialog: List<String> = emptyList(),
    val brandLoadErrorForDialog: String? = null,

    val isLoadingModelsForDialog: Boolean = false,
    val modelsForDialog: List<String> = emptyList(),
    val modelLoadErrorForDialog: String? = null,
    val selectedBrandInDialog: String? = null, // Marca seleccionada DENTRO del diálogo
    val selectedModelInDialog: String? = null, // Modelo seleccionado DENTRO del diálogo

    // Campos para el diálogo de filtros avanzados
    val advancedFilterMinYearInput: String = "",
    val advancedFilterMaxPriceInput: String = "",
    val advancedFilterLocationInput: String = "" // Para ciudad o comunidad en el diálogo
)
