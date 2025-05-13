package com.carlosalcina.drivelist.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.domain.model.AuthError
import com.carlosalcina.drivelist.domain.model.GoogleSignInError
import com.carlosalcina.drivelist.domain.repository.AuthRepository
import com.carlosalcina.drivelist.domain.repository.GoogleSignInHandler
import com.carlosalcina.drivelist.ui.view.states.LoginUiState
import com.carlosalcina.drivelist.utils.Result
import com.carlosalcina.drivelist.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.carlosalcina.drivelist.BuildConfig


class LoginViewModel(
    private val authRepository: AuthRepository,
    private val googleSignInHandler: GoogleSignInHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val googleServerClientId = BuildConfig.GOOGLE_SERVER_CLIENT_ID

    fun onEmailChanged(newEmail: String) {
        val trimmedEmail = newEmail.take(30).trim() // Limitar longitud y quitar espacios
        _uiState.update { currentState ->
            currentState.copy(
                email = trimmedEmail,
                emailError = Utils.validarEmail(trimmedEmail)
            )
        }
        updateCanLoginState()
    }

    fun onPasswordChanged(newPassword: String) {
        val trimmedPassword = newPassword.take(20).trim()
        _uiState.update { currentState ->
            currentState.copy(
                password = trimmedPassword,
                passwordError = Utils.validarPassword(trimmedPassword)
            )
        }
        updateCanLoginState()
    }

    private fun updateCanLoginState() {
        _uiState.update { currentState ->
            val hayErrores = listOf(
                currentState.emailError,
                currentState.passwordError
            ).any { it != null }

            val camposVacios = listOf(
                currentState.email,
                currentState.password
            ).any { it.isBlank() }

            currentState.copy(
                canLogin = !currentState.isLoading && !hayErrores && !camposVacios
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
                            generalMessage = mapAuthErrorToMessage(result.error)
                        )
                    }
                }
            }
        }
    }

    fun iniciarSesionConGoogle(context: Context) { // El contexto de la Activity/Composable
        _uiState.update { it.copy(isLoading = true, generalMessage = null, loginSuccess = false) }

        viewModelScope.launch {
            when (val tokenResult = googleSignInHandler.getGoogleIdToken(context, googleServerClientId)) {
                is Result.Success -> {
                    val idToken = tokenResult.data
                    // Ahora autenticar con Firebase usando el token
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
                                    generalMessage = mapAuthErrorToMessage(authResult.error)
                                )
                            }
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalMessage = mapGoogleSignInErrorToMessage(tokenResult.error)
                        )
                    }
                }
            }
        }
    }

    // Llamar a esto después de que la UI haya manejado el evento de login exitoso
    fun onLoginSuccessEventConsumed() {
        _uiState.update { it.copy(loginSuccess = false, generalMessage = null) }
    }

    // Funciones helper para mapear errores a mensajes amigables
    private fun mapAuthErrorToMessage(error: AuthError): String {
        return when (error) {
            is AuthError.InvalidCredentials -> error.message ?: "Credenciales inválidas."
            is AuthError.NetworkError -> error.message ?: "Error de red."
            is AuthError.UserNotFoundError -> error.message ?: "Usuario no encontrado."
            is AuthError.UnknownError -> error.message ?: "Error desconocido."
            is AuthError.EmailAlreadyInUse -> error.message ?: "Error desconocido."
            is AuthError.WeakPassword -> error.message ?: "Error desconocido."
        }
    }

    private fun mapGoogleSignInErrorToMessage(error: GoogleSignInError): String {
        return when (error) {
            is GoogleSignInError.ApiError -> error.message ?: "Error con la API de Google."
            is GoogleSignInError.NoCredentialFound -> error.message ?: "No se encontraron credenciales de Google."
            is GoogleSignInError.UnexpectedCredentialType -> error.message ?: "Tipo de credencial de Google inesperado."
            GoogleSignInError.UserCancelled -> "Inicio de sesión con Google cancelado."
            is GoogleSignInError.UnknownError -> error.message ?: "Error desconocido con Google Sign-In."
        }
    }
}
