package com.carlosalcina.drivelist.domain.model

import java.util.UUID

data class CarForSale(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val brand: String,
    val model: String,
    val bodyType: String,
    val fuelType: String,
    val year: String,
    val version: String,
    val price: Double,
    val mileage: Int,
    val description: String,
    val imageUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)