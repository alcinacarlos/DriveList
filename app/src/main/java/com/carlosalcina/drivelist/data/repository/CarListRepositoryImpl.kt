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
        private const val YEAR_FIELD = "year"
        private const val COMUNIDAD_AUTONOMA_FIELD = "comunidadAutonoma"
        private const val CIUDAD_FIELD = "ciudad"
        private const val PRICE_FIELD = "price"
        private const val BRAND_FIELD = "brand"
        private const val MODEL_FIELD = "model"
        private const val FUEL_TYPE_FIELD = "fuelType"
        private const val TIMESTAMP_FIELD = "timestamp"
        private const val KEYWORDS_FIELD = "searchableKeywords"
    }

    private suspend fun mapSnapshotToCarList(
        snapshot: QuerySnapshot,
        currentUserId: String?
    ): List<CarForSale> {
        val favoriteCarIds = if (currentUserId != null) {
            when (val favResult = userFavoriteRepository.getUserFavoriteCarIds(currentUserId)) {
                is Result.Success -> favResult.data.toSet()
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
            Log.d("CarListRepo_Debug", "Procesando documento ID: ${document.id}")
            try {
                val car = document.toObject<CarForSale>()?.copy(
                    id = document.id,
                    isFavoriteByCurrentUser = favoriteCarIds.contains(document.id)
                )
                if (car == null) {
                    Log.w(
                        "CarListRepo_Debug",
                        "document.toObject<CarForSale>() devolvió null para ID: ${document.id}"
                    )
                }
                car
            } catch (e: Exception) {
                Log.e("CarListRepo_Debug", "Excepción al convertir documento ID: ${document.id}", e)
                null
            }
        }
    }

    override suspend fun getLatestCars(
        limit: Int,
        currentUserId: String?
    ): Result<List<CarForSale>, Exception> {
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
            var q: Query = firestore.collection(CARS_COLLECTION)

            filters.brand?.takeIf { it.isNotBlank() }?.let { q = q.whereEqualTo(BRAND_FIELD, it) }
            filters.model?.takeIf { it.isNotBlank() }?.let { q = q.whereEqualTo(MODEL_FIELD, it) }
            filters.fuelType?.takeIf { it.isNotBlank() }
                ?.let { q = q.whereEqualTo(FUEL_TYPE_FIELD, it) }
            filters.comunidadAutonoma?.takeIf { it.isNotBlank() }
                ?.let { q = q.whereEqualTo(COMUNIDAD_AUTONOMA_FIELD, it) }
            filters.ciudad?.takeIf { it.isNotBlank() }?.let { q = q.whereEqualTo(CIUDAD_FIELD, it) }
            filters.maxPrice?.let { q = q.whereLessThanOrEqualTo(PRICE_FIELD, it) }
            filters.minYear?.let { q = q.whereGreaterThanOrEqualTo(YEAR_FIELD, it.toString()) }

            filters.searchTerm?.takeIf { it.isNotBlank() }?.let { term ->
                q = q.whereArrayContainsAny(KEYWORDS_FIELD, listOf(term))
            }

            q = q.orderBy(TIMESTAMP_FIELD, Query.Direction.DESCENDING).limit(limit.toLong())

            val snapshot = q.get().await()
            val cars = mapSnapshotToCarList(snapshot, currentUserId)
            Result.Success(cars)
        } catch (e: Exception) {
            Log.e("CarListRepo", "Error searching cars", e)
            Result.Error(e)
        }
    }


    /**
     * Obtiene los detalles de un coche específico por su ID.
     * La lógica para determinar isFavoriteByCurrentUser se mantiene.
     * El objeto CarForSale que se devuelve ahora puede contener sellerDisplayName y sellerProfilePictureUrl
     * si se guardaron al subir el coche.
     */
    override suspend fun getCarById(
        carId: String,
        currentUserId: String?
    ): Result<CarForSale, Exception> {
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
                        when (val favResult =
                            userFavoriteRepository.isCarFavorite(currentUserId, car.id)) {
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