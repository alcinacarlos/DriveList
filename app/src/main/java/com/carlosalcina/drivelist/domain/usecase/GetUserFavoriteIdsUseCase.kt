package com.carlosalcina.drivelist.domain.usecase

import com.carlosalcina.drivelist.domain.repository.UserFavoriteRepository
import javax.inject.Inject
import com.carlosalcina.drivelist.utils.Result

class GetUserFavoriteIdsUseCase @Inject constructor(
    private val userFavoriteRepository: UserFavoriteRepository
) {
    suspend operator fun invoke(userId: String): Result<List<String>, Exception> {
        return userFavoriteRepository.getUserFavoriteCarIds(userId)
    }
}