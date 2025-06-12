package com.carlosalcina.drivelist.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.BuildConfig
import com.carlosalcina.drivelist.domain.repository.AuthRepository
import com.carlosalcina.drivelist.domain.repository.GoogleSignInHandler
import com.carlosalcina.drivelist.ui.states.LoginUiState
import com.carlosalcina.drivelist.utils.Result
import com.carlosalcina.drivelist.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInHandler: GoogleSignInHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val googleServerClientId = BuildConfig.GOOGLE_SERVER_CLIENT_ID

    fun onEmailChanged(newEmail: String) {
        val trimmedEmail = newEmail.take(30).trim()
        _uiState.update { currentState ->
            currentState.copy(
                email = trimmedEmail,
            )
        }
        updateCanLoginState()
    }

    fun onPasswordChanged(newPassword: String) {
        val trimmedPassword = newPassword.take(20).trim()
        _uiState.update { currentState ->
            currentState.copy(
                password = trimmedPassword,
            )
        }
        updateCanLoginState()
    }

    private fun updateCanLoginState() {
        _uiState.update { currentState ->

            val camposVacios = listOf(
                currentState.email,
                currentState.password
            ).any { it.isBlank() }

            currentState.copy(
                canLogin = !currentState.isLoading && !camposVacios
            )
        }
    }

    fun iniciarSesionConEmailPassword() {
        val currentState = _uiState.value
        if (!currentState.canLogin) {
            _uiState.update { it.copy(generalMessage = "Corrige los errores antes de continuar.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, generalMessage = null, loginSuccess = false) }

        viewModelScope.launch {
            when (val result = authRepository.signInWithEmailAndPassword(currentState.email, currentState.password)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalMessage = "Inicio exitoso",
                            loginSuccess = true
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalMessage = Utils.mapAuthErrorToMessage(result.error)
                        )
                    }
                }
            }
        }
    }

    fun iniciarSesionConGoogle(context: Context) {
        _uiState.update { it.copy(isLoading = true, generalMessage = null, loginSuccess = false) }

        viewModelScope.launch {
            when (val tokenResult = googleSignInHandler.getGoogleIdToken(context, googleServerClientId)) {
                is Result.Success -> {
                    val idToken = tokenResult.data
                    when (val authResult = authRepository.signInWithGoogleToken(idToken)) {
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    generalMessage = "Inicio con Google exitoso",
                                    loginSuccess = true
                                )
                            }
                        }
                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    generalMessage = Utils.mapAuthErrorToMessage(authResult.error)
                                )
                            }
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalMessage = Utils.mapGoogleSignInErrorToMessage(tokenResult.error)
                        )
                    }
                }
            }
        }
    }

    fun onLoginSuccessEventConsumed() {
        _uiState.update { it.copy(loginSuccess = false, generalMessage = null) }
    }

    fun onOpenForgotPasswordDialog() {
        _uiState.update {
            it.copy(
                showForgotPasswordDialog = true,
                forgotPasswordEmailInput = it.email,
                passwordResetFeedbackMessage = null,
                isSendingPasswordReset = false
            )
        }
    }

    // Llamado para cerrar el diálogo
    fun onCloseForgotPasswordDialog() {
        _uiState.update {
            it.copy(
                showForgotPasswordDialog = false,
            )
        }
    }

    // Llamado cuando cambia el texto en el campo de email del diálogo
    fun onForgotPasswordDialogEmailChanged(newEmail: String) {
        _uiState.update {
            it.copy(
                forgotPasswordEmailInput = newEmail,
                passwordResetFeedbackMessage = null // Limpiar feedback si el usuario empieza a escribir
            )
        }
    }

    // Llamado cuando el usuario presiona "Enviar" en el diálogo
    fun sendPasswordResetEmailFromDialog() {
        val emailInDialog = _uiState.value.forgotPasswordEmailInput

        if (emailInDialog.isBlank()) {
            _uiState.update {
                it.copy(passwordResetFeedbackMessage = "Por favor, introduce tu correo electrónico.")
            }
            return
        }
        val emailValidationError = Utils.validarEmail(emailInDialog)
        if (emailValidationError != null) {
            _uiState.update {
                it.copy(passwordResetFeedbackMessage = "Por favor, introduce un correo electrónico válido.")
            }
            return
        }

        _uiState.update {
            it.copy(
                isSendingPasswordReset = true,
                passwordResetFeedbackMessage = null
            )
        }

        viewModelScope.launch {
            when (val result = authRepository.sendPasswordResetEmail(emailInDialog)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isSendingPasswordReset = false,
                            passwordResetFeedbackMessage = "Se ha enviado un correo a $emailInDialog. Revisa tu bandeja de entrada."
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSendingPasswordReset = false,
                            passwordResetFeedbackMessage = Utils.mapAuthErrorToMessage(result.error)
                        )
                    }
                }
            }
        }
    }

}