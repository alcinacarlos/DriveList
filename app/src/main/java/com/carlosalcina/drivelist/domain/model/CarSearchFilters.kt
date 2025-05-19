package com.carlosalcina.drivelist.domain.model

data class CarSearchFilters(
    val brand: String? = null,
    val model: String? = null,
    val maxPrice: Double? = null,
    val fuelType: String? = null,
    val comunidadAutonoma: String? = null,
    val ciudad: String? = null
)