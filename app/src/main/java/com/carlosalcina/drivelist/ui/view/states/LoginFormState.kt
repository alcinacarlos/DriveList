package com.carlosalcina.drivelist.ui.view.states

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val generalMessage: String? = null,
    val canLogin: Boolean = false,
    val loginSuccess: Boolean = false // Para manejar la navegación/evento de éxito
)