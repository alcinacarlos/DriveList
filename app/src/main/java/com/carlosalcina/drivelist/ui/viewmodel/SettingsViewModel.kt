package com.carlosalcina.drivelist.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.data.preferences.ThemeRepository
import com.carlosalcina.drivelist.domain.error.AuthError
import com.carlosalcina.drivelist.domain.repository.AuthRepository // Usaremos el repositorio que ya tienes
import com.carlosalcina.drivelist.ui.states.PasswordResetState
import com.carlosalcina.drivelist.ui.theme.ThemeOption
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val themeRepository: ThemeRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _passwordResetState = MutableStateFlow(PasswordResetState())
    val passwordResetState = _passwordResetState.asStateFlow()

    fun onThemeChange(newTheme: ThemeOption) {
        viewModelScope.launch {
            themeRepository.setTheme(newTheme)
        }
    }

    fun sendPasswordResetEmail() {
        val userEmail = firebaseAuth.currentUser?.email

        if (userEmail.isNullOrBlank()) {
            _passwordResetState.update {
                it.copy(errorMessage = "No se pudo encontrar tu correo electrónico para el restablecimiento.")
            }
            return
        }

        viewModelScope.launch {
            _passwordResetState.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }
            when (val result = authRepository.sendPasswordResetEmail(userEmail)) {
                is Result.Success -> {
                    Log.d("SettingsVM", "Correo de restablecimiento enviado a $userEmail")
                    _passwordResetState.update {
                        it.copy(isLoading = false, successMessage = "Correo de restablecimiento enviado a $userEmail")
                    }
                }
                is Result.Error -> {
                    val errorMsg = when (val error = result.error) {
                        is AuthError.UserNotFoundError -> "No se encontró ninguna cuenta para este correo electrónico."
                        else ->  "Ocurrió un error desconocido."
                    }
                    Log.e("SettingsVM", "Error al enviar correo de restablecimiento: $errorMsg")
                    _passwordResetState.update {
                        it.copy(isLoading = false, errorMessage = errorMsg)
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _passwordResetState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}

