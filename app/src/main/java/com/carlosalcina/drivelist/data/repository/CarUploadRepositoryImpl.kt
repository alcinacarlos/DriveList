package com.carlosalcina.drivelist.data.repository

import com.carlosalcina.drivelist.data.datasource.CarRemoteDataSource
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.utils.Result
import javax.inject.Inject

class CarUploadRepositoryImpl @Inject constructor(
    private val remoteDataSource: CarRemoteDataSource
) : CarUploadRepository {

    override suspend fun getBrands(): Result<List<String>, Exception> {
        return remoteDataSource.fetchBrands()
    }

    override suspend fun getModels(brandName: String): Result<List<String>, Exception> {
        return remoteDataSource.fetchModels(brandName)
    }

    override suspend fun getBodyTypes(brandName: String, modelName: String): Result<List<String>, Exception> {
        return remoteDataSource.fetchBodyTypes(brandName, modelName)
    }

    override suspend fun getFuelTypes(
        brandName: String,
        modelName: String,
        bodyTypeName: String
    ): Result<List<String>, Exception> {
        return remoteDataSource.fetchFuelTypes(brandName, modelName, bodyTypeName)
    }

    override suspend fun getYears(
        brandName: String,
        modelName: String,
        bodyTypeName: String,
        fuelTypeName: String
    ): Result<List<String>, Exception> {
        return remoteDataSource.fetchYears(brandName, modelName, bodyTypeName, fuelTypeName)
    }

    override suspend fun getVersions(
        brandName: String,
        modelName: String,
        bodyTypeName: String,
        fuelTypeName: String,
        yearName: String
    ): Result<List<String>, Exception> {
        return remoteDataSource.fetchVersions(brandName, modelName, bodyTypeName, fuelTypeName, yearName)
    }

    override suspend fun uploadCar(car: CarForSale): Result<Unit, Exception> {
        return remoteDataSource.saveCarToFirestore(car)
    }
}