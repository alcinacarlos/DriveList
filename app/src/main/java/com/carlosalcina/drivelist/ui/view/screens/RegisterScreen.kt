package com.carlosalcina.drivelist.ui.view.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.ui.view.components.ButtonAuth
import com.carlosalcina.drivelist.ui.view.components.GoogleSignInButton
import com.carlosalcina.drivelist.ui.viewmodel.RegisterViewModel
import com.carlosalcina.drivelist.utils.Utils

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegister: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var cargando = viewModel.cargando

    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.estadoMensaje) {
        if (viewModel.estadoMensaje == "Registro exitoso") {
            onRegister()
        }
    }

    val hayErrores = listOf(
        viewModel.emailError,
        viewModel.passwordError,
        viewModel.nombreError
    ).any { it != null }

    val camposVacios = listOf(
        viewModel.email,
        viewModel.password,
        viewModel.nombre
    ).any { it.isBlank() }

    val puedeRegistrarse = !viewModel.cargando && !hayErrores && !camposVacios
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text("Registro", style = MaterialTheme.typography.headlineLarge)
                }

                Spacer(modifier = Modifier.height(40.dp))

                OutlinedTextField(
                    shape = RoundedCornerShape(16.dp),
                    value = viewModel.nombre,
                    onValueChange = {
                        viewModel.nombre = it
                        viewModel.nombreError = Utils.validarNombre(it)
                    },
                    label = { Text("Nombre completo") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    isError = viewModel.nombreError != null,
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                viewModel.nombreError?.let {
                    Text(text = it, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    shape = RoundedCornerShape(16.dp),
                    value = viewModel.email,
                    onValueChange = {
                        viewModel.email = it
                        viewModel.emailError = Utils.validarEmail(it)
                    },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                    isError = viewModel.emailError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                viewModel.emailError?.let {
                    Text(text = it, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.password,
                    shape = RoundedCornerShape(16.dp),
                    onValueChange = {
                        viewModel.password = it
                        viewModel.passwordError = Utils.validarPassword(it)
                    },
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
                    isError = viewModel.passwordError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                viewModel.passwordError?.let {
                    Text(text = it, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))

                ButtonAuth(
                    onClick = { viewModel.registrarUsuario() },
                    enabled = puedeRegistrarse && !cargando
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text("Registrarse")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GoogleSignInButton(onClick = onGoogleSignIn)

                viewModel.estadoMensaje?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        it,
                        color = if ("exitoso" in it.lowercase()) Color.Green else Color.Red
                    )
                }
            }

        }
    }
}
