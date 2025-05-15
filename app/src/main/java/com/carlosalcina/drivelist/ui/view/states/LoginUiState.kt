package com.carlosalcina.drivelist.ui.view.states

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val generalMessage: String? = null,
    val canLogin: Boolean = false,
    val loginSuccess: Boolean = false,
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordEmailInput: String = "", // Email ingresado en el diálogo
    val isSendingPasswordReset: Boolean = false, // Estado de carga para el envío del correo
    val passwordResetFeedbackMessage: String? = null
)