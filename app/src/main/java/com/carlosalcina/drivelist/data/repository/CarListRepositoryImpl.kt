package com.carlosalcina.drivelist.data.repository

import android.util.Log
import com.carlosalcina.drivelist.data.remote.MeiliSearchApi
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.CarSearchFilters
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.domain.repository.UserFavoriteRepository
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.firestore.FieldPath
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
        private const val YEAR_FIELD = "year"
        private const val COMUNIDAD_AUTONOMA_FIELD = "comunidadAutonoma"
        private const val CIUDAD_FIELD = "ciudad"
        private const val PRICE_FIELD = "price"
        private const val BRAND_FIELD = "brand"
        private const val MODEL_FIELD = "model"
        private const val FUEL_TYPE_FIELD = "fuelType"
        private const val TIMESTAMP_FIELD = "timestamp"
    }

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
        try {
            Log.d("CarListRepo", "Iniciando búsqueda con filtros: $filters, limit: $limit")

            val searchTermProvided = !filters.searchTerm.isNullOrBlank()
            val carIdsFromSearch = mutableListOf<String>()

            if (searchTermProvided) {
                Log.d("CarListRepo", "Buscando en MeiliSearch con término: '${filters.searchTerm}'")
                val response = meiliSearchApi.searchCars(filters.searchTerm, limit = limit * 2)
                carIdsFromSearch.addAll(response.hits.map { it.id })
                Log.d("CarListRepo", "MeiliSearch encontró ${carIdsFromSearch.size} IDs: $carIdsFromSearch")

                if (carIdsFromSearch.isEmpty()) {
                    Log.d("CarListRepo", "MeiliSearch no encontró resultados para '${filters.searchTerm}'. Devolviendo lista vacía.")
                    return Result.Success(emptyList())
                }
            }

            val hasOtherFilters = filters.brand != null || filters.model != null ||
                    filters.fuelType != null || filters.comunidadAutonoma != null ||
                    filters.ciudad != null || (filters.maxPrice != null && filters.maxPrice > 0) ||
                    filters.minYear != null

            if (!searchTermProvided && !hasOtherFilters) {
                Log.d("CarListRepo", "No se proporcionó término de búsqueda ni otros filtros. Devolviendo lista vacía.")
                return Result.Success(emptyList())
            }

            var query: Query = firestore.collection(CARS_COLLECTION)

            // Aplicar filtros de Firestore
            filters.brand?.let { query = query.whereEqualTo(BRAND_FIELD, it) }
            filters.model?.let { query = query.whereEqualTo(MODEL_FIELD, it) }
            filters.fuelType?.let { query = query.whereEqualTo(FUEL_TYPE_FIELD, it) }
            filters.comunidadAutonoma?.let { query = query.whereEqualTo(COMUNIDAD_AUTONOMA_FIELD, it) }
            filters.ciudad?.let { query = query.whereEqualTo(CIUDAD_FIELD, it) }

            filters.maxPrice?.let {
                if (it > 0) query = query.whereLessThanOrEqualTo(PRICE_FIELD, it)
            }
            filters.minYear?.let {
                query = query.whereGreaterThanOrEqualTo(YEAR_FIELD, it)
            }


            if (carIdsFromSearch.isNotEmpty()) {
                val idsToFilter = carIdsFromSearch.take(30)
                if (idsToFilter.isNotEmpty()){
                    query = query.whereIn(FieldPath.documentId(), idsToFilter)
                    Log.d("CarListRepo", "Aplicando filtro whereIn de Firestore con ${idsToFilter.size} IDs.")
                }
            }

            query = if (!searchTermProvided && hasOtherFilters) {
                if (filters.maxPrice != null && filters.maxPrice > 0) {
                    query.orderBy(PRICE_FIELD, Query.Direction.ASCENDING)
                } else if (filters.minYear != null) {
                    query.orderBy(YEAR_FIELD, Query.Direction.DESCENDING)
                } else {
                    query.orderBy(TIMESTAMP_FIELD, Query.Direction.DESCENDING)
                }
            } else if (carIdsFromSearch.isNotEmpty()) {
                query.orderBy(TIMESTAMP_FIELD, Query.Direction.DESCENDING)
            } else {
                query.orderBy(TIMESTAMP_FIELD, Query.Direction.DESCENDING)
            }


            val finalQuery = query.limit(limit.toLong())
            Log.d("CarListRepo", "Ejecutando consulta final a Firestore.")

            val snapshot = finalQuery.get().await()
            val cars = mapSnapshotToCarList(snapshot, currentUserId)
            Log.d("CarListRepo", "Búsqueda completada. Encontrados ${cars.size} coches.")
            return Result.Success(cars)

        } catch (e: Exception) {
            Log.e("CarListRepo", "Error buscando coches con filtros: $filters. Error: ${e.message}", e)
            return Result.Error(e)
        }
    }
    /**
     * Obtiene los detalles de un coche específico por su ID.
     * La lógica para determinar isFavoriteByCurrentUser se mantiene.
     * El objeto CarForSale que se devuelve ahora puede contener sellerDisplayName y sellerProfilePictureUrl
     * si se guardaron al subir el coche.
     */
    override suspend fun getCarById(carId: String, currentUserId: String?): Result<CarForSale, Exception> {
        return try {
            if (carId.isBlank()) {
                return Result.Error(IllegalArgumentException("Car ID cannot be blank."))
            }
            val documentSnapshot = firestore.collection(CARS_COLLECTION)
                .document(carId)
                .get()
                .await()

            if (documentSnapshot.exists()) {
                val car = documentSnapshot.toObject<CarForSale>()?.copy(
                    id = documentSnapshot.id
                )

                if (car != null) {
                    val isFavorite = if (currentUserId != null) {
                        when (val favResult = userFavoriteRepository.isCarFavorite(currentUserId, car.id)) {
                            is Result.Success -> favResult.data
                            is Result.Error -> {
                                false
                            }
                        }
                    } else {
                        false
                    }
                    Result.Success(car.copy(isFavoriteByCurrentUser = isFavorite))
                } else {
                    Result.Error(Exception("Failed to parse car data."))
                }
            } else {
                Result.Error(Exception("Car not found."))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}