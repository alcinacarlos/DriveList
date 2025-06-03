package com.carlosalcina.drivelist.ui.view.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.domain.model.UserData
import com.carlosalcina.drivelist.ui.states.EditableField
import com.carlosalcina.drivelist.ui.states.ProfileScreenUiState
import com.carlosalcina.drivelist.ui.view.components.CarCardWithoutFavorite
import com.carlosalcina.drivelist.ui.viewmodel.ProfileViewModel
import com.carlosalcina.drivelist.utils.Utils.formatUserSinceDetail

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onCarClicked: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Manejar mensajes de éxito/error para la actualización de campos
    LaunchedEffect(uiState.fieldUpdateSuccessMessage) {
        uiState.fieldUpdateSuccessMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.clearUpdateMessages()
        }
    }
    LaunchedEffect(uiState.fieldUpdateErrorMessage) {
        uiState.fieldUpdateErrorMessage?.let {
            snackbarHostState.showSnackbar(message = "Error: $it", duration = SnackbarDuration.Long)
            viewModel.clearUpdateMessages()
        }
    }
    LaunchedEffect(uiState.photoUploadErrorMessage) {
        uiState.photoUploadErrorMessage?.let {
            snackbarHostState.showSnackbar(
                message = "Error foto: $it", duration = SnackbarDuration.Long
            )
            viewModel.clearUpdateMessages()
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "Ocurrió un error desconocido.",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        } else if (uiState.authUser == null || uiState.userData == null) {
            Text(
                "Datos de usuario no disponibles.",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        } else {
            ProfileContent(
                uiState = uiState, viewModel = viewModel, onCarClicked = onCarClicked
            )
        }

        // Diálogo para editar un campo individual
        if (uiState.editingField != null) {
            SingleFieldEditDialog(
                uiState = uiState,
                onDismiss = { viewModel.onDismissEditFieldDialog() },
                onSave = { viewModel.saveFieldChange() },
                onValueChange = { viewModel.onEditableFieldValueChanged(it) })
        }

        // Diálogo de opciones para la foto de perfil
        if (uiState.showPhotoOptionsDialog) {
            PhotoOptionsDialog(
                viewModel = viewModel, onDismiss = { viewModel.onDismissPhotoOptionsDialog() })
        }

    }
}

@Composable
fun ProfileContent(
    uiState: ProfileScreenUiState, viewModel: ProfileViewModel, onCarClicked: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            ProfileHeader(
                userData = uiState.userData,
                authUserEmail = uiState.authUser?.email,
                isUploadingPhoto = uiState.isUploadingPhoto,
                onEditPhotoClicked = { viewModel.onProfilePhotoEditClicked() },
                onEditField = { field -> viewModel.onEditField(field) })
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Coches en Venta",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (uiState.isLoadingCars) {
            item { CircularProgressIndicator(modifier = Modifier.padding(vertical = 20.dp)) }
        } else if (uiState.carsErrorMessage != null) {
            item {
                Text(
                    text = "Error al cargar coches: ${uiState.carsErrorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }
        } else if (uiState.userCars.isEmpty()) {
            item {
                Text(
                    text = "Aún no has publicado ningún coche.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }
        } else {
            items(uiState.userCars, key = { car -> car.id }) { car ->
                CarCardWithoutFavorite(
                    car = car,
                    onClick = { onCarClicked(car.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun ProfileHeader(
    userData: UserData?,
    authUserEmail: String?,
    isUploadingPhoto: Boolean,
    onEditPhotoClicked: () -> Unit,
    onEditField: (EditableField) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Box {
                Image(
                    painter = rememberAsyncImagePainter(model = userData?.photoURL.takeIf { !it.isNullOrBlank() }
                    ?: R.drawable.no_photo,
                    error = painterResource(id = R.drawable.no_photo)),
                    contentDescription = "Foto de Perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentScale = ContentScale.Crop)
                if (isUploadingPhoto) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onEditPhotoClicked,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (8).dp, y = (8).dp)
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Editar Foto de Perfil",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        EditableProfileField(
            label = "Nombre",
            value = userData?.displayName ?: authUserEmail ?: "N/A",
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            onEditClicked = { onEditField(EditableField.DISPLAY_NAME) })

        Text(
            text = userData?.email ?: authUserEmail ?: "Sin email",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        EditableProfileField(
            label = "Biografía",
            value = userData?.bio ?: "Sin biografía",
            placeholder = "Añade una biografía...",
            onEditClicked = { onEditField(EditableField.BIO) })
        Spacer(modifier = Modifier.height(12.dp))
        Text(formatUserSinceDetail(userData?.createdAt), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

    }
}

@Composable
fun EditableProfileField(
    label: String,
    value: String,
    placeholder: String = "Pulsa para editar",
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    onEditClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onEditClicked), // Hacer toda la fila clickeable
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(
                text = value.ifBlank { placeholder },
                style = textStyle.copy(
                    color = if (value.isBlank()) MaterialTheme.colorScheme.outline else LocalContentColor.current
                ),
                maxLines = if (label.equals("Biografía", ignoreCase = true)) 3 else 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onEditClicked) {
            Icon(
                Icons.Filled.Edit,
                contentDescription = "Editar $label",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SingleFieldEditDialog(
    uiState: ProfileScreenUiState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onValueChange: (String) -> Unit
) {
    val fieldToEdit = uiState.editingField ?: return
    val label = when (fieldToEdit) {
        EditableField.DISPLAY_NAME -> "Nombre"
        EditableField.BIO -> "Biografía"
        EditableField.PHOTO_URL -> "URL de la foto"
    }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Editar $label") }, text = {
        Column {
            OutlinedTextField(
                value = uiState.fieldValueToEdit,
                onValueChange = onValueChange,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = fieldToEdit != EditableField.BIO,
                maxLines = if (fieldToEdit == EditableField.BIO) 3 else 1
            )
            if (uiState.isUpdatingField) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            uiState.fieldUpdateErrorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }, confirmButton = {
        Button(onClick = onSave, enabled = !uiState.isUpdatingField) {
            Text("Guardar")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancelar")
        }
    })
}

@Composable
fun PhotoOptionsDialog(
    viewModel: ProfileViewModel, onDismiss: () -> Unit
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onNewProfilePhotoSelected(uri)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Foto de Perfil",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                PhotoOptionItem(
                    icon = Icons.Filled.PhotoCamera, text = "Seleccionar desde galería", onClick = {
                        imagePickerLauncher.launch("image/*")
                        // onDismiss() // Se cierra desde el ViewModel al procesar
                    })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                PhotoOptionItem(
                    icon = Icons.Filled.DeleteOutline, text = "Borrar foto de perfil", onClick = {
                        viewModel.onDeleteProfilePhoto()
                        // onDismiss() // Se cierra desde el ViewModel
                    }, contentColor = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
fun PhotoOptionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    contentColor: Color = LocalContentColor.current
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, tint = contentColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, color = contentColor, style = MaterialTheme.typography.bodyLarge)
    }
}
