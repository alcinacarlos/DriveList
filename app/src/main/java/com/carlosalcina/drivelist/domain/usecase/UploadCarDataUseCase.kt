package com.carlosalcina.drivelist.domain.usecase

import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.utils.Result
import javax.inject.Inject

class UploadCarDataUseCase @Inject constructor(
    private val repository: CarUploadRepository
) {
    suspend operator fun invoke(car: CarForSale): Result<Unit, Exception> = repository.uploadCar(car)
}