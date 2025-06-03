package com.carlosalcina.drivelist.data.datasource

import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.utils.Result

interface CarRemoteDataSource {
    suspend fun fetchBrands(): Result<List<String>, Exception>
    suspend fun fetchModels(brandName: String): Result<List<String>, Exception>
    suspend fun fetchBodyTypes(brandName: String, modelName: String): Result<List<String>, Exception>
    suspend fun fetchFuelTypes(brandName: String, modelName: String, bodyTypeName: String): Result<List<String>, Exception>
    suspend fun fetchYears(brandName: String, modelName: String, bodyTypeName: String, fuelTypeName: String): Result<List<String>, Exception>
    suspend fun fetchVersions(brandName: String, modelName: String, bodyTypeName: String, fuelTypeName: String, yearName: String): Result<List<String>, Exception>

    suspend fun saveCarToFirestore(car: CarForSale): Result<Unit, Exception>
    suspend fun editCarFromFirestore(car: CarForSale): Result<Unit, Exception>
}