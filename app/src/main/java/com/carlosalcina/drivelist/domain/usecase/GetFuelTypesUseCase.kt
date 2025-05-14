package com.carlosalcina.drivelist.domain.usecase

import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.utils.Result
import javax.inject.Inject

class GetFuelTypesUseCase @Inject constructor(
    private val repository: CarUploadRepository
) {
    suspend operator fun invoke(
        brandName: String,
        modelName: String,
        bodyTypeName: String
    ): Result<List<String>, Exception> = repository.getFuelTypes(brandName, modelName, bodyTypeName)
}