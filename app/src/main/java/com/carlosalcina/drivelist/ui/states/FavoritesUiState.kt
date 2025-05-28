package com.carlosalcina.drivelist.ui.states

import com.carlosalcina.drivelist.domain.model.CarForSale

data class FavoritesUiState(
    val favoriteCars: List<CarForSale> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTogglingFavorite: Map<String, Boolean> = emptyMap(),
    val isAuthenticated: Boolean = false
)