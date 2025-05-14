package com.carlosalcina.drivelist.domain.usecase

import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.utils.Result
import javax.inject.Inject

class GetBodyTypesUseCase @Inject constructor(
    private val repository: CarUploadRepository
) {
    suspend operator fun invoke(brandName: String, modelName: String): Result<List<String>, Exception> =
        repository.getBodyTypes(brandName, modelName)
}