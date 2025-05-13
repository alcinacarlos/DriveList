package com.carlosalcina.drivelist.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.BuildConfig
import com.carlosalcina.drivelist.domain.model.AuthError
import com.carlosalcina.drivelist.domain.model.GoogleSignInError
import com.carlosalcina.drivelist.domain.repository.AuthRepository
import com.carlosalcina.drivelist.domain.repository.GoogleSignInHandler
import com.carlosalcina.drivelist.ui.view.states.RegisterUiState
import com.carlosalcina.drivelist.utils.Result
import com.carlosalcina.drivelist.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val googleSignInHandler: GoogleSignInHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val googleServerClientId = BuildConfig.GOOGLE_SERVER_CLIENT_ID

    fun onEmailChanged(newEmail: String) {
        val trimmedEmail = newEmail.take(30).trim()
        _uiState.update { currentState ->
            currentState.copy(
                email = trimmedEmail,
                emailError = Utils.validarEmail(trimmedEmail)
            )
        }
        updateCanRegisterState()
    }

    fun onPasswordChanged(newPassword: String) {
        val trimmedPassword = newPassword.take(20).trim()
        _uiState.update { currentState ->
            currentState.copy(
                password = trimmedPassword,
                passwordError = Utils.validarPassword(trimmedPassword)
            )
        }
        updateCanRegisterState()
    }

    fun onNameChanged(newName: String) {
        val trimmedName = newName.take(30).trim() // Limitar longitud y quitar espacios
        _uiState.update { currentState ->
            currentState.copy(
                nombre = trimmedName,
                nombreError = Utils.validarNombre(trimmedName)
            )
        }
        updateCanRegisterState()
    }

    private fun updateCanRegisterState() {
        _uiState.update { currentState ->
            val hayErrores = listOfNotNull( // listOfNotNull para ignorar nulos
                currentState.emailError,
                currentState.passwordError,
                currentState.nombreError
            ).any() // .any() es suficiente si los errores son strings no vacíos

            val camposVacios = listOf(
                currentState.email,
                currentState.password,
                currentState.nombre
            ).any { it.isBlank() }

            currentState.copy(
                canRegister = !currentState.isLoading && !hayErrores && !camposVacios
            )
        }
    }

    fun registrarConEmailPassword() {
        val currentState = _uiState.value
        if (!currentState.canRegister) {
            _uiState.update { it.copy(generalMessage = "Corrige los errores antes de continuar.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, generalMessage = null, registrationSuccess = false) }

        viewModelScope.launch {
            when (val result = authRepository.createUserWithEmailAndPassword(
                currentState.email,
                currentState.password,
                currentState.nombre
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalMessage = "Registro exitoso. Bienvenido ${result.data.displayName ?: currentState.nombre}!",
                            registrationSuccess = true
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

    // Para Google, el registro y login son el mismo flujo en Firebase si el usuario no existe.
    // El nombre se tomará del perfil de Google.
    fun registrarOIniciarSesionConGoogle(context: Context) {
        _uiState.update { it.copy(isLoading = true, generalMessage = null, registrationSuccess = false) }

        viewModelScope.launch {
            when (val tokenResult = googleSignInHandler.getGoogleIdToken(context, googleServerClientId)) {
                is Result.Success -> {
                    val idToken = tokenResult.data
                    when (val authResult = authRepository.signInWithGoogleToken(idToken)) {
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    // El mensaje podría indicar si es nuevo usuario o no, si authResult lo proveyera
                                    generalMessage = "Inicio/Registro con Google exitoso. Bienvenido ${authResult.data.displayName ?: ""}!",
                                    registrationSuccess = true // Usamos el mismo flag de éxito
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

    fun onRegistrationSuccessEventConsumed() {
        _uiState.update { it.copy(registrationSuccess = false, generalMessage = null) }
    }

    private fun mapAuthErrorToMessage(error: AuthError): String {
        return when (error) {
            is AuthError.EmailAlreadyInUse -> error.message ?: "El correo ya está registrado."
            is AuthError.WeakPassword -> error.message ?: "La contraseña es muy débil."
            is AuthError.InvalidCredentials -> error.message ?: "Credenciales inválidas." // Aunque no aplica tanto a registro
            is AuthError.NetworkError -> error.message ?: "Error de red."
            is AuthError.UserNotFoundError -> error.message ?: "Usuario no encontrado." // No aplica a registro
            is AuthError.UnknownError -> error.message ?: "Error desconocido."
        }
    }

    private fun mapGoogleSignInErrorToMessage(error: GoogleSignInError): String {
        return when (error) {
            is GoogleSignInError.ApiError -> error.message ?: "Error con la API de Google."
            is GoogleSignInError.NoCredentialFound -> error.message ?: "No se encontraron credenciales de Google."
            is GoogleSignInError.UnexpectedCredentialType -> error.message ?: "Tipo de credencial de Google inesperado."
            GoogleSignInError.UserCancelled -> "Registro/Inicio con Google cancelado."
            is GoogleSignInError.UnknownError -> error.message ?: "Error desconocido con Google Sign-In."
        }
    }
}