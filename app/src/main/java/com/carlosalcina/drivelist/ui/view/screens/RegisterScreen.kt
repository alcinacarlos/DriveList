package com.carlosalcina.drivelist.ui.view.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.ui.viewmodel.RegisterViewModel
import com.carlosalcina.drivelist.utils.Utils

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegister: () -> Unit
) {
    var fotoUrl = viewModel.fotoUrl
    var cargando = viewModel.cargando

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.fotoUrl = uri.toString()
        }
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Registro", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(110.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (fotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = fotoUrl,
                            contentDescription = "Foto seleccionada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .clickable { launcher.launch("image/*") }
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_avatar_placeholder),
                            contentDescription = "Avatar por defecto",
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .clickable { launcher.launch("image/*") }
                        )
                    }

                    IconButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier
                            .size(22.dp)
                            .offset(x = (-3).dp, y = (-3).dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Editar foto",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = viewModel.nombre,
                    onValueChange = {
                        viewModel.nombre = it
                        viewModel.nombreError = Utils.validarNombre(it)
                    },
                    label = { Text("Nombre completo") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    isError = viewModel.nombreError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                viewModel.nombreError?.let {
                    Text(text = it, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = {
                        viewModel.email = it
                        viewModel.emailError = Utils.validarEmail(it)
                    },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                    isError = viewModel.emailError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                viewModel.emailError?.let {
                    Text(text = it, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.password,
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
                    modifier = Modifier.fillMaxWidth()
                )
                viewModel.passwordError?.let {
                    Text(text = it, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.registrarUsuario() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = puedeRegistrarse && !cargando,
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

                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continuar con Google", color = Color.Black)
                }

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
