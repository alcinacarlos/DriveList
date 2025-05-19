package com.carlosalcina.drivelist.domain.usecase

import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.CarSearchFilters
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.utils.Result
import javax.inject.Inject

class SearchCarsUseCase @Inject constructor(
    private val carListRepository: CarListRepository
) {
    suspend operator fun invoke(filters: CarSearchFilters, limit: Int = 20, currentUserId: String?): Result<List<CarForSale>, Exception> {
        return carListRepository.searchCars(filters, limit, currentUserId)
    }
}
