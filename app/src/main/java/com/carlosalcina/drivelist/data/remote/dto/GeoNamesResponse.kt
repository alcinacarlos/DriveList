package com.carlosalcina.drivelist.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GeoNamesResponse(
    @SerializedName("postalCodes")
    val postalCodes: List<PostalCodeInfo>?
)