package com.carlosalcina.drivelist.data.repository

import android.util.Log
import com.carlosalcina.drivelist.data.remote.MeiliSearchApi
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
    private val userFavoriteRepository: UserFavoriteRepository,
    private val meiliSearchApi: MeiliSearchApi
) : CarListRepository {

    companion object {
        private const val CARS_COLLECTION = "coches_venta"
        private const val SEARCHABLE_KEYWORDS_FIELD = "searchableKeywords"
        private const val YEAR_FIELD = "year" // Nombre del campo año en Firestore
        private const val COMUNIDAD_AUTONOMA_FIELD = "comunidadAutonoma"
        private const val CIUDAD_FIELD = "ciudad"
        private const val PRICE_FIELD = "price"
        private const val BRAND_FIELD = "brand"
        private const val MODEL_FIELD = "model"
        private const val FUEL_TYPE_FIELD = "fuelType"
        private const val TIMESTAMP_FIELD = "timestamp"
    }

    /**
     * Helper para convertir un QuerySnapshot de Firestore a List<CarForSale>,
     * incluyendo la determinación del estado 'isFavoriteByCurrentUser'.
     * (Este helper se mantiene igual que en la versión anterior)
     */
    private suspend fun mapSnapshotToCarList(
        snapshot: QuerySnapshot,
        currentUserId: String?
    ): List<CarForSale> {
        val favoriteCarIds = if (currentUserId != null) {
            when (val favResult = userFavoriteRepository.getUserFavoriteCarIds(currentUserId)) {
                is Result.Success -> favResult.data.toSet()
                is Result.Error -> {
                    Log.e("CarListRepo", "Failed to fetch user favorites: ${favResult.error.message}")
                    emptySet()
                }
            }
        } else {
            emptySet()
        }

        return snapshot.documents.mapNotNull { document ->
            Log.d("CarListRepo_Debug", "Procesando documento ID: ${document.id}")
            try {
                val carData = document.data // Obtener los datos crudos como un mapa
                Log.d("CarListRepo_Debug", "Datos del documento: $carData")
                val car = document.toObject<CarForSale>()?.copy(
                    id = document.id,
                    isFavoriteByCurrentUser = favoriteCarIds.contains(document.id)
                )
                if (car == null) {
                    Log.w("CarListRepo_Debug", "document.toObject<CarForSale>() devolvió null para ID: ${document.id}")
                }
                car
            } catch (e: Exception) {
                Log.e("CarListRepo_Debug", "Excepción al convertir documento ID: ${document.id}", e)
                null
            }
        }
    }

    override suspend fun getLatestCars(limit: Int, currentUserId: String?): Result<List<CarForSale>, Exception> {
        // Esta función se mantiene igual que antes
        return try {
            val snapshot = firestore.collection(CARS_COLLECTION)
                .orderBy(TIMESTAMP_FIELD, Query.Direction.DESCENDING)
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
        return try {
            val carIdsFromSearch = mutableListOf<String>()

            // Buscar primero con MeiliSearch si hay término de búsqueda
            if (!filters.searchTerm.isNullOrBlank()) {
                val response = meiliSearchApi.searchCars(filters.searchTerm, limit)
                carIdsFromSearch.addAll(response.hits.map { it.id })
            }

            var query: Query = firestore.collection(CARS_COLLECTION)

            filters.brand?.let { query = query.whereEqualTo(BRAND_FIELD, it) }
            filters.model?.let { query = query.whereEqualTo(MODEL_FIELD, it) }
            filters.fuelType?.let { query = query.whereEqualTo(FUEL_TYPE_FIELD, it) }
            filters.comunidadAutonoma?.let { query = query.whereEqualTo(COMUNIDAD_AUTONOMA_FIELD, it) }
            filters.ciudad?.let { query = query.whereEqualTo(CIUDAD_FIELD, it) }

            filters.maxPrice?.let {
                if (it > 0) query = query.whereLessThanOrEqualTo(PRICE_FIELD, it)
            }
            filters.minYear?.let {
                query = query.whereGreaterThanOrEqualTo(YEAR_FIELD, it.toString())
            }

            // Si hay resultados de MeiliSearch, filtrar por ellos
            if (carIdsFromSearch.isNotEmpty()) {
                query = query.whereIn("id", carIdsFromSearch.take(10)) // Firestore limita a 10 elementos en whereIn
            }

            // Ordenar si no hay término de búsqueda (para evitar conflicto con whereIn)
            if (filters.searchTerm.isNullOrBlank()) {
                if (filters.maxPrice != null && filters.maxPrice > 0) {
                    query = query.orderBy(PRICE_FIELD, Query.Direction.ASCENDING)
                } else if (filters.minYear != null) {
                    query = query.orderBy(YEAR_FIELD, Query.Direction.DESCENDING)
                }

                try {
                    query = query.orderBy(TIMESTAMP_FIELD, Query.Direction.DESCENDING)
                } catch (e: Exception) {
                    Log.w("CarListRepo", "No se pudo aplicar orden por timestamp: ${e.message}")
                }
            }

            val snapshot = query.limit(limit.toLong()).get().await()
            val cars = mapSnapshotToCarList(snapshot, currentUserId)
            Result.Success(cars)
        } catch (e: Exception) {
            Log.e("CarListRepo", "Error searching cars with filters: $filters", e)
            Result.Error(e)
        }
    }

}