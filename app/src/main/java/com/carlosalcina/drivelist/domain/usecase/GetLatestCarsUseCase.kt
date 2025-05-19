package com.carlosalcina.drivelist.domain.usecase

import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.utils.Result
import javax.inject.Inject

/**
 * Caso de uso para obtener los últimos coches publicados.
 * Ahora acepta currentUserId.
 */
class GetLatestCarsUseCase @Inject constructor(
    private val carListRepository: CarListRepository
) {
    suspend operator fun invoke(limit: Int = 20, currentUserId: String?): Result<List<CarForSale>, Exception> {
        if (limit <= 0) return Result.Error(IllegalArgumentException("El límite debe ser mayor que cero."))
        return carListRepository.getLatestCars(limit, currentUserId)
    }
}