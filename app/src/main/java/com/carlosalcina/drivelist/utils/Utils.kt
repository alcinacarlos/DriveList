package com.carlosalcina.drivelist.utils

import android.util.Patterns
import com.carlosalcina.drivelist.domain.model.AuthError
import com.carlosalcina.drivelist.domain.model.GoogleSignInError

object Utils {

    fun validarEmail(email: String): String? {
        return if (email.isBlank()) {
            "El correo no puede estar vacío"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Formato de correo inválido"
        } else null
    }

    fun validarPassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña no puede estar vacía"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            !password.any { it.isDigit() } -> "Debe contener al menos un número"
            !password.any { it.isUpperCase() } -> "Debe contener una mayúscula"
            else -> null
        }
    }

    fun validarNombre(nombre: String): String? {
        return if (nombre.isBlank()) {
            "El nombre no puede estar vacío"
        } else if (nombre.length < 3) {
            "El nombre debe tener al menos 3 letras"
        } else null
    }

    // Funciones helper para mapear errores a mensajes amigables
    fun mapAuthErrorToMessage(error: AuthError): String {
        return when (error) {
            is AuthError.InvalidCredentials -> error.message ?: "Credenciales inválidas."
            is AuthError.NetworkError -> error.message ?: "Error de red."
            is AuthError.UserNotFoundError -> error.message ?: "Usuario no encontrado."
            is AuthError.UnknownError -> error.message ?: "Error desconocido."
            is AuthError.EmailAlreadyInUse -> error.message ?: "Error desconocido."
            is AuthError.WeakPassword -> error.message ?: "Error desconocido."
        }
    }

    fun mapGoogleSignInErrorToMessage(error: GoogleSignInError): String {
        return when (error) {
            is GoogleSignInError.ApiError -> error.message ?: "Error con la API de Google."
            is GoogleSignInError.NoCredentialFound -> error.message ?: "No se encontraron credenciales de Google."
            is GoogleSignInError.UnexpectedCredentialType -> error.message ?: "Tipo de credencial de Google inesperado."
            GoogleSignInError.UserCancelled -> "Inicio de sesión con Google cancelado."
            is GoogleSignInError.UnknownError -> error.message ?: "Error desconocido con Google Sign-In."
        }
    }

    fun parseFuel(fuel: String): String{
        return when(fuel){
            "Diésel" -> "Disel"
            "Híbrido" -> "Hbrido"
            "Híbrido enchufable" -> "Hbridoenchufable"
            else -> {"Gasolina"}
        }
    }
}