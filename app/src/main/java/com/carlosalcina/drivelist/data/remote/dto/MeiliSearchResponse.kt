package com.carlosalcina.drivelist.data.remote.dto

data class MeiliSearchResponse(
    val hits: List<CarHit>
)

data class CarHit(
    val id: String,
    val brand: String,
    val model: String,
    val version: String,
    val price: Double
)