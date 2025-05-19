package com.carlosalcina.drivelist.data.repository

import android.util.Log
import com.carlosalcina.drivelist.domain.repository.UserFavoriteRepository
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class UserFavoriteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserFavoriteRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val FAVORITES_SUBCOLLECTION = "favorites"
        private const val FAVORITED_AT_FIELD = "favoritedAt"
    }

    override suspend fun addFavorite(userId: String, carId: String): Result<Unit, Exception> {
        return try {
            val favoriteData = mapOf(FAVORITED_AT_FIELD to System.currentTimeMillis())
            firestore.collection(USERS_COLLECTION).document(userId)
                .collection(FAVORITES_SUBCOLLECTION).document(carId)
                .set(favoriteData) // Guardar con un timestamp o un documento vacío
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("UserFavoriteRepo", "Error adding favorite for user $userId, car $carId", e)
            Result.Error(e)
        }
    }

    override suspend fun removeFavorite(userId: String, carId: String): Result<Unit, Exception> {
        return try {
            firestore.collection(USERS_COLLECTION).document(userId)
                .collection(FAVORITES_SUBCOLLECTION).document(carId)
                .delete()
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("UserFavoriteRepo", "Error removing favorite for user $userId, car $carId", e)
            Result.Error(e)
        }
    }

    override suspend fun getUserFavoriteCarIds(userId: String): Result<List<String>, Exception> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION).document(userId)
                .collection(FAVORITES_SUBCOLLECTION)
                .get()
                .await()
            val favoriteCarIds = snapshot.documents.map { it.id }
            Result.Success(favoriteCarIds)
        } catch (e: Exception) {
            Log.e("UserFavoriteRepo", "Error getting favorite car IDs for user $userId", e)
            Result.Error(e)
        }
    }

    override suspend fun isCarFavorite(userId: String, carId: String): Result<Boolean, Exception> {
        return try {
            val documentSnapshot = firestore.collection(USERS_COLLECTION).document(userId)
                .collection(FAVORITES_SUBCOLLECTION).document(carId)
                .get()
                .await()
            Result.Success(documentSnapshot.exists())
        } catch (e: Exception) {
            Log.e("UserFavoriteRepo", "Error checking if car $carId is favorite for user $userId", e)
            Result.Error(e)
        }
    }
}