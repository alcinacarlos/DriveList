package com.carlosalcina.drivelist.utils

import android.util.Patterns

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
}