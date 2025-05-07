package com.carlosalcina.drivelist.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import com.carlosalcina.drivelist.utils.FirebaseUtils
import com.carlosalcina.drivelist.utils.Utils
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterViewModel : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var nombre by mutableStateOf("")
    var fotoUrl by mutableStateOf("")

    var emailError by mutableStateOf<String?>(null)
    var passwordError by mutableStateOf<String?>(null)
    var nombreError by mutableStateOf<String?>(null)

    var estadoMensaje by mutableStateOf<String?>(null)
    var cargando by mutableStateOf(false)

    private val auth = FirebaseUtils.getInstance()

    fun registrarUsuario() {
        // Validar campos
        emailError = Utils.validarEmail(email)
        passwordError = Utils.validarPassword(password)
        nombreError = Utils.validarNombre(nombre)

        if (emailError != null || passwordError != null || nombreError != null) {
            estadoMensaje = "Corrige los errores antes de continuar."
            return
        }

        cargando = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    subirFotoYGuardarUrl()
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .apply {
                            if (fotoUrl.isNotBlank()) {
                                photoUri = fotoUrl.toUri()
                            }
                        }
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener {
                            estadoMensaje = "Registro exitoso"
                        }
                } else {
                    estadoMensaje = task.exception?.message ?: "Error desconocido"
                }
                cargando = false
            }
    }

    fun subirFotoYGuardarUrl() {
        if (fotoUrl.isBlank()) return
        val uri = fotoUrl.toUri()

        val uid = auth.currentUser?.uid ?: return
        val storageRef = FirebaseUtils.getStorage().reference
        val fileRef = storageRef.child("users/$uid/profile.jpg")

        cargando = true
        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { url ->
                    fotoUrl = url.toString()
                    estadoMensaje = "Foto subida correctamente"
                    cargando = false
                }
            }
            .addOnFailureListener {
                estadoMensaje = "Error al subir la foto: ${it.message}"
                cargando = false
            }
    }

    fun iniciarSesionConCredentialManager(
        context: Context,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("1063637134638-v816om6gg24utetk2dspjth8bhrk62b1.apps.googleusercontent.com")
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = withContext(Dispatchers.IO) {
                    credentialManager.getCredential(context, request)
                }

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    autenticarConGoogle(idToken, onSuccess, onError)
                } else {
                    onError("Tipo de credencial inesperado")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error al iniciar sesión con Google")
            }
        }
    }

    private fun autenticarConGoogle(
        idToken: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        cargando = true
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                cargando = false
                if (it.isSuccessful) {
                    estadoMensaje = "Inicio con Google exitoso"
                    onSuccess()
                } else {
                    estadoMensaje = it.exception?.message ?: "Error de autenticación con Google"
                    onError(estadoMensaje!!)
                }
            }
    }
}
