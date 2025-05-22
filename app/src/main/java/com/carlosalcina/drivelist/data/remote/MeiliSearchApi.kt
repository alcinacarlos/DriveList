package com.carlosalcina.drivelist.data.remote

import com.carlosalcina.drivelist.data.remote.dto.MeiliSearchResponse
import com.carlosalcina.drivelist.data.remote.dto.MeiliTaskResponse
import com.carlosalcina.drivelist.domain.model.CarForSale
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MeiliSearchApi {
    @GET("indexes/cars/search")
    suspend fun searchCars(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): MeiliSearchResponse

    @POST("indexes/cars/documents")
    suspend fun addOrUpdateCars(
        @Body cars: List<CarForSale>
    ): MeiliTaskResponse

    @DELETE("indexes/cars/documents")
    suspend fun deleteCars(
        @Body ids: List<String>
    ): MeiliTaskResponse
}