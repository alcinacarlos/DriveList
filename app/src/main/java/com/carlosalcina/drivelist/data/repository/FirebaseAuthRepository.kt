package com.carlosalcina.drivelist.data.repository

import com.carlosalcina.drivelist.domain.model.*
import com.carlosalcina.drivelist.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(private val firebaseAuth: FirebaseAuth) : AuthRepository { // Inyectar FirebaseAuth
    override suspend fun signInWithEmailAndPassword(email: String, password: String): Result<AuthUser, AuthError> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await() // Usar kotlinx-coroutines-play-services
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                Result.Success(AuthUser(firebaseUser.uid, firebaseUser.email))
            } else {
                Result.Error(AuthError.UnknownError("Usuario no encontrado después del login."))
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.Error(AuthError.UserNotFoundError(e.message))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.Error(AuthError.InvalidCredentials(e.message))
        } catch (e: Exception) {
            Result.Error(AuthError.UnknownError(e.message ?: "Error desconocido en login con email."))
        }
    }

    override suspend fun signInWithGoogleToken(idToken: String): Result<AuthUser, AuthError> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                Result.Success(AuthUser(firebaseUser.uid, firebaseUser.email))
            } else {
                Result.Error(AuthError.UnknownError("Usuario no encontrado después del login con Google."))
            }
        } catch (e: Exception) {
            Result.Error(AuthError.UnknownError(e.message ?: "Error de autenticación con Google."))
        }
    }

    override suspend fun createUserWithEmailAndPassword(email: String, password: String, displayName: String): Result<AuthUser, AuthError> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                // Actualizar el perfil con el nombre
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await() // Esperar a que se complete la actualización

                Result.Success(AuthUser(firebaseUser.uid, firebaseUser.email, firebaseUser.displayName))
            } else {
                Result.Error(AuthError.UnknownError("Usuario no encontrado después del registro."))
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.Error(AuthError.EmailAlreadyInUse(e.message ?: "El correo electrónico ya está en uso."))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.Error(AuthError.WeakPassword(e.message ?: "La contraseña es demasiado débil."))
        } catch (e: Exception) {
            Result.Error(AuthError.UnknownError(e.message ?: "Error desconocido durante el registro."))
        }
    }
}