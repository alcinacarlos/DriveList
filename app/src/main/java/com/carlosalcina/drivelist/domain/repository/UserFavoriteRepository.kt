package com.carlosalcina.drivelist.domain.repository
import com.carlosalcina.drivelist.utils.Result

interface UserFavoriteRepository {
    /**
     * Añade un coche a la lista de favoritos de un usuario.
     * @param userId ID del usuario.
     * @param carId ID del coche a añadir.
     * @return Result con Unit en caso de éxito o una Exception en caso de error.
     */
    suspend fun addFavorite(userId: String, carId: String): Result<Unit, Exception>

    /**
     * Elimina un coche de la lista de favoritos de un usuario.
     * @param userId ID del usuario.
     * @param carId ID del coche a eliminar.
     * @return Result con Unit en caso de éxito o una Exception en caso de error.
     */
    suspend fun removeFavorite(userId: String, carId: String): Result<Unit, Exception>

    /**
     * Obtiene una lista de los IDs de los coches que un usuario ha marcado como favoritos.
     * @param userId ID del usuario.
     * @return Result con una lista de IDs de coches o una Exception.
     */
    suspend fun getUserFavoriteCarIds(userId: String): Result<List<String>, Exception>

    /**
     * Comprueba si un coche específico es favorito para un usuario.
     * (Alternativa o complemento a getUserFavoriteCarIds, puede ser más directo para un solo coche)
     * @param userId ID del usuario.
     * @param carId ID del coche.
     * @return Result con un Boolean o una Exception.
     */
    suspend fun isCarFavorite(userId: String, carId: String): Result<Boolean, Exception>
}