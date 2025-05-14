package com.carlosalcina.drivelist.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.carlosalcina.drivelist.domain.model.GoogleSignInError
import com.carlosalcina.drivelist.domain.repository.GoogleSignInHandler
import com.carlosalcina.drivelist.utils.Result
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CredentialManagerGoogleSignInHandler @Inject constructor() : GoogleSignInHandler {
    override suspend fun getGoogleIdToken(context: Context, serverClientId: String): Result<String, GoogleSignInError> {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = withContext(Dispatchers.IO) { // Importante para operaciones de E/S
                credentialManager.getCredential(context, request)
            }
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Result.Success(googleIdTokenCredential.idToken)
            } else {
                Result.Error(GoogleSignInError.UnexpectedCredentialType("Tipo de credencial inesperado: ${credential?.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.Error(GoogleSignInError.UserCancelled)
        } catch (e: NoCredentialException) {
            Result.Error(GoogleSignInError.NoCredentialFound(e.message))
        }
        catch (e: Exception) {
            Result.Error(GoogleSignInError.UnknownError(e.message ?: "Error al obtener credencial de Google."))
        }
    }
}
