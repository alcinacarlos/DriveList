package com.carlosalcina.drivelist.ui.states

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val nombre: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val nombreError: String? = null,
    val isLoading: Boolean = false,
    val generalMessage: String? = null,
    val canRegister: Boolean = false,
    val registrationSuccess: Boolean = false // Para manejar la navegación/evento de éxito
)