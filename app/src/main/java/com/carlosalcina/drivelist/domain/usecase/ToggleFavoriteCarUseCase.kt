package com.carlosalcina.drivelist.domain.usecase

import com.carlosalcina.drivelist.domain.repository.UserFavoriteRepository
import com.carlosalcina.drivelist.utils.Result
import javax.inject.Inject

class ToggleFavoriteCarUseCase @Inject constructor(
    private val userFavoriteRepository: UserFavoriteRepository
) {
    /**
     * @param userId ID del usuario actual.
     * @param carId ID del coche a marcar/desmarcar como favorito.
     * @param isCurrentlyFavorite El estado actual de favorito del coche.
     * @return Result con Unit en caso de éxito o una Exception.
     */
    suspend operator fun invoke(userId: String, carId: String, isCurrentlyFavorite: Boolean): Result<Unit, Exception> {
        return if (isCurrentlyFavorite) {
            userFavoriteRepository.removeFavorite(userId, carId)
        } else {
            userFavoriteRepository.addFavorite(userId, carId)
        }
    }
}