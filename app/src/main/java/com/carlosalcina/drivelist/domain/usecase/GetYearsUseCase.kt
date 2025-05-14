package com.carlosalcina.drivelist.domain.usecase

import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.utils.Result
import javax.inject.Inject

class GetYearsUseCase @Inject constructor(
    private val repository: CarUploadRepository
) {
    suspend operator fun invoke(
        brandName: String,
        modelName: String,
        bodyTypeName: String,
        fuelTypeName: String
    ): Result<List<String>, Exception> = repository.getYears(brandName, modelName, bodyTypeName, fuelTypeName)
}