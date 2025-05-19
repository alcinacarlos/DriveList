package com.carlosalcina.drivelist.domain.repository

import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.CarSearchFilters
import com.carlosalcina.drivelist.utils.Result

/**
 * Repositorio para obtener listas de coches.
 * Ahora los métodos aceptan un currentUserId opcional para determinar el estado de 'favorito'.
 */
interface CarListRepository {
    /**
     * Obtiene los últimos coches publicados.
     * @param limit El número máximo de coches a obtener.
     * @param currentUserId El ID del usuario actual para determinar si los coches son favoritos.
     * Si es null, isFavoriteByCurrentUser será false.
     */
    suspend fun getLatestCars(limit: Int, currentUserId: String?): Result<List<CarForSale>, Exception>

    /**
     * Busca coches según los filtros aplicados.
     * @param filters Los filtros de búsqueda.
     * @param limit El número máximo de coches a obtener.
     * @param currentUserId El ID del usuario actual.
     */
    suspend fun searchCars(filters: CarSearchFilters, limit: Int, currentUserId: String?): Result<List<CarForSale>, Exception>
}
