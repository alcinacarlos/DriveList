package com.carlosalcina.drivelist.data.repository

import com.carlosalcina.drivelist.domain.model.AuthError
import com.carlosalcina.drivelist.domain.model.AuthUser
import com.carlosalcina.drivelist.domain.model.FirestoreError
import com.carlosalcina.drivelist.domain.model.UserData
import com.carlosalcina.drivelist.domain.repository.AuthRepository
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth, private val firestore: FirebaseFirestore
) : AuthRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun signInWithEmailAndPassword(
        email: String, password: String
    ): Result<AuthUser, AuthError> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                Result.Success(
                    AuthUser(
                        firebaseUser.uid, firebaseUser.email, firebaseUser.displayName
                    )
                )
            } else {
                Result.Error(AuthError.UnknownError("Usuario no encontrado después del login."))
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.Error(AuthError.UserNotFoundError(e.message))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.Error(AuthError.InvalidCredentials(e.message))
        } catch (e: Exception) {
            Result.Error(AuthError.UnknownError(e.message ?: "Error desconocido en login."))
        }
    }

    override suspend fun signInWithGoogleToken(idToken: String): Result<AuthUser, AuthError> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                // Guardar o actualizar datos del usuario en Firestore
                val userDocRef = usersCollection.document(firebaseUser.uid)
                val documentSnapshot = userDocRef.get().await()

                val userData: UserData
                if (!documentSnapshot.exists()) {
                    userData = UserData(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email,
                        displayName = firebaseUser.displayName,
                        photoURL = firebaseUser.photoUrl?.toString(),
                        createdAt = Timestamp.now()
                    )
                    userDocRef.set(userData).await()
                } else {
                    userDocRef.update(
                        mapOf(
                            "displayName" to firebaseUser.displayName,
                            "photoURL" to firebaseUser.photoUrl?.toString(),
                        )
                    ).await()
                }

                Result.Success(
                    AuthUser(
                        firebaseUser.uid, firebaseUser.email, firebaseUser.displayName
                    )
                )
            } else {
                Result.Error(AuthError.UnknownError("Usuario Google no encontrado."))
            }
        } catch (e: Exception) {
            Result.Error(AuthError.UnknownError(e.message ?: "Error con Google Sign-In."))
        }
    }

    override suspend fun createUserWithEmailAndPassword(
        email: String, password: String, displayName: String
    ): Result<AuthUser, AuthError> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val profileUpdates =
                    UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
                firebaseUser.updateProfile(profileUpdates).await()

                // Guardar en Firestore
                val userData = UserData(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = displayName,
                    createdAt = Timestamp.now(),
                    photoURL = null
                )
                usersCollection.document(firebaseUser.uid).set(userData).await()

                Result.Success(
                    AuthUser(
                        firebaseUser.uid, firebaseUser.email, displayName
                    )
                )
            } else {
                Result.Error(AuthError.UnknownError("Usuario no encontrado tras registro."))
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.Error(AuthError.EmailAlreadyInUse(e.message))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.Error(AuthError.WeakPassword(e.message))
        } catch (e: Exception) {
            Result.Error(AuthError.UnknownError(e.message ?: "Error desconocido en registro."))
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit, AuthError> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.Error(AuthError.UserNotFoundError("Email no registrado."))
        } catch (e: Exception) {
            Result.Error(AuthError.UnknownError(e.message))
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override fun getCurrentFirebaseUser(): AuthUser? {
        val firebaseUser = firebaseAuth.currentUser
        return firebaseUser?.let {
            AuthUser(it.uid, it.email, it.displayName)
        }
    }

    override suspend fun getUserData(uid: String): Result<UserData, FirestoreError> {
        return try {
            val documentSnapshot = usersCollection.document(uid).get().await()
            val userData = documentSnapshot.toObject<UserData>()
            if (userData != null) {
                Result.Success(userData)
            } else {
                Result.Error(FirestoreError.NotFound())
            }
        } catch (e: Exception) {
            Result.Error(FirestoreError.Unknown(e.message ?: "Error al obtener datos de usuario."))
        }
    }

    override suspend fun getCurrentUserData(): Result<UserData, FirestoreError> {
        val currentUser = firebaseAuth.currentUser
        return if (currentUser != null) {
            getUserData(currentUser.uid)
        } else {
            Result.Error(FirestoreError.NotFound("No hay usuario logueado."))
        }
    }

    override suspend fun updateCurrentUserData(dataToUpdate: Map<String, Any>): Result<Unit, FirestoreError> {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            return Result.Error(FirestoreError.NotFound("No hay usuario logueado para actualizar."))
        }
        return try {
            if (dataToUpdate.containsKey("displayName")) {
                val newDisplayName = dataToUpdate["displayName"] as? String
                if (newDisplayName != null) {
                    val profileUpdates =
                        UserProfileChangeRequest.Builder().setDisplayName(newDisplayName).build()
                    currentUser.updateProfile(profileUpdates).await()
                }
            }
            if (dataToUpdate.containsKey("email")) {
                val newEmail = dataToUpdate["email"] as? String
                if (newEmail != null) {
                    currentUser.updateEmail(newEmail).await()
                }
            }


            usersCollection.document(currentUser.uid).update(dataToUpdate).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(FirestoreError.OperationFailed(e.message ?: "Error al actualizar datos."))
        }
    }

    override suspend fun deleteCurrentAccountAndData(): Result<Unit, AuthError> {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            return Result.Error(AuthError.UserNotFoundError("No hay usuario logueado para eliminar."))
        }

        return try {
            val uidToDelete = currentUser.uid
            currentUser.delete().await()

            try {
                usersCollection.document(uidToDelete).delete().await()
            } catch (e: Exception) {
                Result.Error(AuthError.UnknownError(e.message ?: "Error al eliminar datos."))
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AuthError.UnknownError(e.message ?: "Error al eliminar cuenta."))
        }
    }
}