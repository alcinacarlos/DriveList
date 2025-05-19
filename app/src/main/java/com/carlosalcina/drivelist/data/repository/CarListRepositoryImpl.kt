package com.carlosalcina.drivelist.data.repository

import android.util.Log
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.CarSearchFilters
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.domain.repository.UserFavoriteRepository
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CarListRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userFavoriteRepository: UserFavoriteRepository
) : CarListRepository {

    companion object {
        private const val CARS_COLLECTION = "coches_venta"
    }

    /**
     * Helper para convertir un QuerySnapshot de Firestore a List<CarForSale>,
     * incluyendo la determinación del estado 'isFavoriteByCurrentUser'.
     */
    private suspend fun mapSnapshotToCarList(
        snapshot: QuerySnapshot,
        currentUserId: String?
    ): List<CarForSale> {
        val favoriteCarIds = if (currentUserId != null) {
            when (val favResult = userFavoriteRepository.getUserFavoriteCarIds(currentUserId)) {
                is Result.Success -> favResult.data.toSet() // Convertir a Set para búsquedas rápidas
                is Result.Error -> {
                    Log.e(
                        "CarListRepo",
                        "Failed to fetch user favorites: ${favResult.error.message}"
                    )
                    emptySet()
                }
            }
        } else {
            emptySet()
        }

        return snapshot.documents.mapNotNull { document ->
            try {
                // Convertir el documento de Firestore al objeto CarForSale del dominio.
                // Firestore necesita que el modelo tenga un constructor vacío y propiedades públicas (o getters/setters).
                // Si tu CarForSale tiene @Transient isFavoriteByCurrentUser, toObject() lo ignorará, lo cual está bien.
                val car = document.toObject<CarForSale>()?.copy(
                    // El ID del documento es el ID del coche
                    id = document.id,
                    // Determinar si es favorito
                    isFavoriteByCurrentUser = favoriteCarIds.contains(document.id)
                )
                car
            } catch (e: Exception) {
                Log.e(
                    "CarListRepo",
                    "Error converting Firestore document to CarForSale: ${document.id}",
                    e
                )
                null // Omitir coches que no se puedan parsear
            }
        }
    }

    override suspend fun getLatestCars(
        limit: Int,
        currentUserId: String?
    ): Result<List<CarForSale>, Exception> {
        return try {
            val snapshot = firestore.collection(CARS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING) // Ordenar por más recientes
                .limit(limit.toLong())
                .get()
                .await()
            val cars = mapSnapshotToCarList(snapshot, currentUserId)
            Result.Success(cars)
        } catch (e: Exception) {
            Log.e("CarListRepo", "Error getting latest cars", e)
            Result.Error(e)
        }
    }

    override suspend fun searchCars(
        filters: CarSearchFilters,
        limit: Int,
        currentUserId: String?
    ): Result<List<CarForSale>, Exception> {
        try {
            var query: Query = firestore.collection(CARS_COLLECTION)

            // Aplicar filtros
            filters.brand?.let { query = query.whereEqualTo("brand", it) }
            filters.model?.let { query = query.whereEqualTo("model", it) }
            filters.fuelType?.let { query = query.whereEqualTo("fuelType", it) }
            filters.comunidadAutonoma?.let { query = query.whereEqualTo("comunidadAutonoma", it) }
            filters.ciudad?.let { query = query.whereEqualTo("ciudad", it) }

            // Filtro de precio máximo: whereLessThanOrEqualTo
            filters.maxPrice?.let {
                if (it > 0) { // Solo aplicar si el precio es positivo
                    query = query.whereLessThanOrEqualTo("price", it)
                }
            }

            // TODO: Firestore requiere un índice compuesto para consultas con múltiples whereEqualTo y orderBy diferentes.
            // Por ahora, ordenaremos por timestamp, pero si combinas muchos filtros y un orderBy
            // en un campo no filtrado, necesitarás índices.
            // Si no hay un filtro de precio, podemos ordenar por precio y luego por timestamp.
            // Si hay filtro de precio, es mejor ordenar por timestamp o relevancia (más complejo).
            if (filters.maxPrice != null && filters.maxPrice > 0) {
                query = query.orderBy(
                    "price",
                    Query.Direction.ASCENDING
                ) // O DESCENDING, según prefieras
            }
            query = query.orderBy("timestamp", Query.Direction.DESCENDING)


            val snapshot = query.limit(limit.toLong()).get().await()
            val cars = mapSnapshotToCarList(snapshot, currentUserId)
            return Result.Success(cars)
        } catch (e: Exception) {
            Log.e("CarListRepo", "Error searching cars with filters: $filters", e)
            return Result.Error(e)
        }
    }
}