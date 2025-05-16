package com.carlosalcina.drivelist.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PostalCodeInfo(
    @SerializedName("postalCode")
    val postalCode: String?,

    @SerializedName("placeName")
    val placeName: String?,

    @SerializedName("adminName1")
    val adminName1: String?,

    @SerializedName("adminName2")
    val adminName2: String?,

)