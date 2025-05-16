package com.carlosalcina.drivelist.data.remote

import com.carlosalcina.drivelist.data.remote.dto.GeoNamesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoNamesApiService {

    @GET("postalCodeSearchJSON")
    suspend fun searchPostalCode(
        @Query("postalcode") postalCode: String,
        @Query("country") countryCode: String = "ES",
        @Query("maxRows") maxRows: Int = 5,
        @Query("username") username: String
    ): Response<GeoNamesResponse>
}