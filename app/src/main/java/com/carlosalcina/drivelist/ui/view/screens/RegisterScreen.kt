package com.carlosalcina.drivelist.ui.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.carlosalcina.drivelist.ui.view.components.ButtonAuth
import com.carlosalcina.drivelist.ui.view.components.GoogleSignInButton
import com.carlosalcina.drivelist.ui.viewmodel.RegisterViewModel


@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateOnSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.registrationSuccess) {
        if (uiState.registrationSuccess) {
            onNavigateOnSuccess()
            viewModel.onRegistrationSuccessEventConsumed() // Limpia el evento
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 20.dp) // Ajuste de padding vertical
                .verticalScroll(rememberScrollState()), // Para que sea scrollable si el contenido es mucho
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text("Registro", style = MaterialTheme.typography.headlineLarge)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Campo Nombre
            OutlinedTextField(
                shape = RoundedCornerShape(16.dp),
                value = uiState.nombre,
                onValueChange = { viewModel.onNameChanged(it) },
                label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                isError = uiState.nombreError != null,
                supportingText = {
                    uiState.nombreError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth().height(90.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Email
            OutlinedTextField(
                shape = RoundedCornerShape(16.dp),
                value = uiState.email,
                onValueChange = { viewModel.onEmailChanged(it) },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                isError = uiState.emailError != null,
                supportingText = {
                    uiState.emailError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth().height(90.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = uiState.password,
                shape = RoundedCornerShape(16.dp),
                onValueChange = { viewModel.onPasswordChanged(it) },
                label = { Text("Contraseña") },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                isError = uiState.passwordError != null,
                supportingText = {
                    uiState.passwordError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth().height(90.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botón Registrarse
            ButtonAuth(
                onClick = { viewModel.registrarConEmailPassword() },
                enabled = uiState.canRegister // canRegister ya considera isLoading
            ) {
                if (uiState.isLoading) { // Mostrar indicador si está cargando por esta acción
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary, // Color para contraste sobre primario
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Registrarse")
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Botón Google Sign In
            GoogleSignInButton(
                onClick = { viewModel.registrarOIniciarSesionConGoogle(context) },
                enabled = !uiState.isLoading // Deshabilitar si cualquier carga está en progreso
            )

            // Mensaje General (éxito/error)
            uiState.generalMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    color = if (uiState.registrationSuccess && message.contains(
                            "exitoso",
                            ignoreCase = true
                        )
                    ) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
    // Botón para ir a Iniciar Sesión
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 30.dp),
        contentAlignment = Alignment.BottomCenter

    ) {
        TextButton(
            onClick = onNavigateToLogin,
            enabled = !uiState.isLoading,
        ) {
            Text("¿Ya tienes cuenta?")
            Spacer(modifier = Modifier.width(5.dp))
            Text("Inicia Sesión", color = MaterialTheme.colorScheme.primary)
        }
    }
}