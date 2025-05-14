package com.carlosalcina.drivelist.domain.usecase

import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.utils.Result
import javax.inject.Inject

class GetBrandsUseCase @Inject constructor(
    private val repository: CarUploadRepository
) {
    suspend operator fun invoke(): Result<List<String>, Exception> = repository.getBrands()
}