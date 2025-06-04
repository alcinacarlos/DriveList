package com.carlosalcina.drivelist.domain.repository

import com.carlosalcina.drivelist.domain.error.AuthError
import com.carlosalcina.drivelist.domain.error.FirestoreError
import com.carlosalcina.drivelist.domain.model.AuthUser
import com.carlosalcina.drivelist.domain.model.UserData
import com.carlosalcina.drivelist.utils.Result

interface AuthRepository {
    // Operaciones de Autenticación
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<AuthUser, AuthError>
    suspend fun signInWithGoogleToken(idToken: String): Result<AuthUser, AuthError>
    suspend fun createUserWithEmailAndPassword(email: String, password: String, displayName: String): Result<AuthUser, AuthError>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit, AuthError>
    suspend fun signOut()

    fun getCurrentFirebaseUser(): AuthUser?
    suspend fun getUserData(uid: String): Result<UserData, FirestoreError>
    suspend fun getCurrentUserData(): Result<UserData, FirestoreError>
    suspend fun updateCurrentUserData(dataToUpdate: Map<String, Any>): Result<Unit, FirestoreError>
    suspend fun deleteCurrentAccountAndData(): Result<Unit, AuthError> // Elimina de Auth y Firestore
}