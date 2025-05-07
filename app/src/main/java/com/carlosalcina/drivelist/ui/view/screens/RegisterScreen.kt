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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.ui.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegister: () -> Unit
) {
    val email = viewModel.email
    val password = viewModel.password
    val nombre = viewModel.nombre
    val fotoUrl = viewModel.fotoUrl
    val estadoMensaje = viewModel.estadoMensaje
    val cargando = viewModel.cargando

    val emailError = viewModel.emailError
    val passwordError = viewModel.passwordError
    val nombreError = viewModel.nombreError

    var passwordVisible by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.fotoUrl = uri.toString()
        }
    }

    LaunchedEffect(estadoMensaje) {
        if (estadoMensaje == "Registro exitoso") {
            onRegister()
        }
    }

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

                // Avatar + botón editar
                Box(
                    modifier = Modifier
                        .size(100.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (fotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = fotoUrl,
                            contentDescription = "Foto seleccionada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .clickable { launcher.launch("image/*") }
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_avatar_placeholder),
                            contentDescription = "Avatar por defecto",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .clickable { launcher.launch("image/*") }
                        )
                    }

                    IconButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier
                            .size(28.dp)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Editar foto",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { if (!it.contains("\n")) viewModel.nombre = it },
                    label = { Text("Nombre completo") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    isError = nombreError != null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                nombreError?.let {
                    Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { if (!it.contains("\n")) viewModel.email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                    isError = emailError != null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
                )
                emailError?.let {
                    Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { if (!it.contains("\n")) viewModel.password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = null)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = passwordError != null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                passwordError?.let {
                    Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.registrarUsuario() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cargando
                ) {
                    Text("Registrarse")
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

                estadoMensaje?.let {
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
