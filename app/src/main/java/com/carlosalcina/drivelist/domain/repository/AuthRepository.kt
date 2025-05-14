package com.carlosalcina.drivelist.domain.repository

import com.carlosalcina.drivelist.domain.model.AuthError
import com.carlosalcina.drivelist.domain.model.AuthUser
import com.carlosalcina.drivelist.utils.Result

interface AuthRepository {
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<AuthUser, AuthError>
    suspend fun signInWithGoogleToken(idToken: String): Result<AuthUser, AuthError>
    suspend fun createUserWithEmailAndPassword(email: String, password: String, displayName: String): Result<AuthUser, AuthError>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit, AuthError>
}