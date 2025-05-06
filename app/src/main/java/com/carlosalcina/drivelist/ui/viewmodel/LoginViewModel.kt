package com.carlosalcina.drivelist.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var estadoMensaje by mutableStateOf<String?>(null)
    var cargando by mutableStateOf(false)

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun iniciarSesion(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            estadoMensaje = "Correo y contraseña obligatorios."
            return
        }

        cargando = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                cargando = false
                estadoMensaje = if (task.isSuccessful) {
                    onSuccess()
                    "Inicio de sesión exitoso"
                } else {
                    task.exception?.message ?: "Error desconocido"
                }
            }
    }
}
