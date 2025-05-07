package com.carlosalcina.drivelist.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import androidx.core.net.toUri
import com.carlosalcina.drivelist.utils.FirebaseUtils
import com.carlosalcina.drivelist.utils.Utils

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
}
