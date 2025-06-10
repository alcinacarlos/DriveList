package com.carlosalcina.drivelist.ui.states

data class PasswordResetState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)