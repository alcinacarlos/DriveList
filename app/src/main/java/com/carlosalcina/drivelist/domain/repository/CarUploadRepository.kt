package com.carlosalcina.drivelist.domain.repository

import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.utils.Result

interface CarUploadRepository {
    // Las listas ahora son de Strings directamente
    suspend fun getBrands(): Result<List<String>, Exception>
    suspend fun getModels(brandName: String): Result<List<String>, Exception>
    suspend fun getBodyTypes(brandName: String, modelName: String): Result<List<String>, Exception>
    suspend fun getFuelTypes(brandName: String, modelName: String, bodyTypeName: String): Result<List<String>, Exception>
    suspend fun getYears(brandName: String, modelName: String, bodyTypeName: String, fuelTypeName: String): Result<List<String>, Exception>
    suspend fun getVersions(brandName: String, modelName: String, bodyTypeName: String, fuelTypeName: String, yearName: String): Result<List<String>, Exception>

    suspend fun uploadCar(car: CarForSale): Result<Unit, Exception>
}