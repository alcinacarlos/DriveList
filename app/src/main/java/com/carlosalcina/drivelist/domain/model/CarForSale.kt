package com.carlosalcina.drivelist.domain.model

import java.util.UUID

data class CarForSale(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val brand: String = "",
    val model: String = "",
    val bodyType: String = "",
    val fuelType: String = "",
    val year: String= "",
    val version: String= "",
    val carColor: String = "",
    val price: Double = 0.0,
    val mileage: Int = 0,
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val comunidadAutonoma: String? = null,
    val ciudad: String? = null,
    val postalCode: String? = null,

    @Transient
    val isFavoriteByCurrentUser: Boolean = false,

    val searchableKeywords: List<String> = emptyList()
)