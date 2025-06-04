package com.carlosalcina.drivelist.ui.view.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.carlosalcina.drivelist.domain.model.CarColor
import com.carlosalcina.drivelist.ui.states.EditCarScreenState
import com.carlosalcina.drivelist.ui.viewmodel.EditCarViewModel

// PANTALLA DE EDICIÓN DE COCHE
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCarScreen(
    navController: NavController,
    viewModel: EditCarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Launcher para el permiso de localización (si reutilizas la lógica de UploadCarViewModel)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) viewModel.onLocationPermissionGranted()
            else viewModel.onLocationPermissionDenied()
        }
    )
//    LaunchedEffect(uiState.isRequestingLocationPermission) { // Asumiendo que tienes este estado
//        if (uiState.isRequestingLocationPermission) {
//            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
//        }
//    }

    // Efectos para mostrar mensajes (errores, éxito)
    LaunchedEffect(uiState.saveCarError, uiState.initialDataLoadError, uiState.imageProcessingMessage, uiState.locationGeneralErrorMessage, uiState.brandLoadError, uiState.modelLoadError) {
        val error = uiState.saveCarError ?: uiState.initialDataLoadError ?: uiState.imageProcessingMessage ?: uiState.locationGeneralErrorMessage ?: uiState.brandLoadError ?: uiState.modelLoadError
        error?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            // Aquí podrías tener una función en el ViewModel para limpiar el error específico
        }
    }
    LaunchedEffect(uiState.saveCarSuccess) {
        if (uiState.saveCarSuccess) {
            snackbarHostState.showSnackbar(
                message = "Anuncio actualizado con éxito.", // Reemplazar con stringResource
                duration = SnackbarDuration.Long
            )
            navController.popBackStack() // Volver a la pantalla anterior (Detalles del Coche)
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Editar Anuncio") }, // Reemplazar con stringResource
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoadingInitialData) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("Cargando datos del coche...", modifier = Modifier.padding(top = 60.dp))
            }
        } else if (!uiState.canEditCar || uiState.initialCarData == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.initialDataLoadError ?: "No se pueden editar los datos de este coche.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            // Contenido del formulario de edición
            EditCarFormContent(
                uiState = uiState,
                viewModel = viewModel,
                context = context,
                focusManager = focusManager,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCarFormContent(
    uiState: EditCarScreenState,
    viewModel: EditCarViewModel,
    context: Context,
    focusManager: androidx.compose.ui.focus.FocusManager,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Launcher para el selector de múltiples imágenes
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(EditCarViewModel.MAX_IMAGES_ALLOWED - (uiState.existingImageUrls.size - uiState.imagesToDelete.size)),
        onResult = { uris -> viewModel.onNewImagesSelected(uris) }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sección de Imágenes
        Text("Imágenes del Vehículo (máx. ${EditCarViewModel.MAX_IMAGES_ALLOWED})", style = MaterialTheme.typography.titleMedium)

        // Mostrar imágenes existentes
        if (uiState.existingImageUrls.isNotEmpty()) {
            Text("Imágenes actuales (toca para marcar/desmarcar para eliminar):", style = MaterialTheme.typography.bodySmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.existingImageUrls, key = { it }) { imageUrl ->
                    ExistingImageItem(
                        imageUrl = imageUrl,
                        isMarkedForDeletion = uiState.imagesToDelete.contains(imageUrl),
                        onToggleDeletion = { viewModel.toggleImageForDeletion(imageUrl) }
                    )
                }
            }
        }

        // Mostrar nuevas imágenes seleccionadas
        if (uiState.selectedImageUris.isNotEmpty()) {
            Text("Nuevas imágenes seleccionadas (toca para quitar):", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.selectedImageUris,  key = { it.toString() }) { uri ->
                    NewImageItem(uri = uri, onRemove = { viewModel.removeSelectedNewImage(uri) })
                }
            }
        }

        // Botón para añadir más imágenes
        val currentTotalImages = uiState.existingImageUrls.size - uiState.imagesToDelete.size + uiState.selectedImageUris.size
        if (currentTotalImages < EditCarViewModel.MAX_IMAGES_ALLOWED) {
            OutlinedButton(
                onClick = {
                    val canAddCount = EditCarViewModel.MAX_IMAGES_ALLOWED - currentTotalImages
                    if (canAddCount > 0) {
                        multiplePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Añadir imágenes")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Añadir nuevas imágenes (${EditCarViewModel.MAX_IMAGES_ALLOWED - currentTotalImages} restantes)")
            }
        }
        if(uiState.isUploadingImages || uiState.imageProcessingMessage != null){
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(uiState.imageProcessingMessage ?: "Procesando imágenes...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }


        // Campos del Formulario
        // Marca
        var brandMenuExpanded by remember { mutableStateOf(false) }
        Text("Marca*", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(expanded = brandMenuExpanded, onExpandedChange = { brandMenuExpanded = !brandMenuExpanded }) {
            OutlinedTextField(
                value = uiState.brand ?: "Seleccionar Marca...",
                onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandMenuExpanded) },
                isError = uiState.brand.isNullOrBlank() && uiState.saveCarError != null // Ejemplo de validación visual
            )
            ExposedDropdownMenu(expanded = brandMenuExpanded, onDismissRequest = { brandMenuExpanded = false }) {
                if (uiState.isLoadingBrands) {
                    DropdownMenuItem(text = { Text("Cargando...") }, onClick = {}, enabled = false)
                } else {
                    uiState.availableBrands.forEach { brand ->
                        DropdownMenuItem(text = { Text(brand) }, onClick = { viewModel.onBrandSelected(brand); brandMenuExpanded = false })
                    }
                }
            }
        }
        if (uiState.brandLoadError != null) Text(uiState.brandLoadError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)


        // Modelo (solo si hay marca seleccionada)
        if (!uiState.brand.isNullOrBlank()) {
            var modelMenuExpanded by remember { mutableStateOf(false) }
            Text("Modelo*", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
            ExposedDropdownMenuBox(expanded = modelMenuExpanded, onExpandedChange = { modelMenuExpanded = !modelMenuExpanded }) {
                OutlinedTextField(
                    value = uiState.model ?: "Seleccionar Modelo...",
                    onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelMenuExpanded) },
                    isError = uiState.model.isNullOrBlank() && uiState.saveCarError != null
                )
                ExposedDropdownMenu(expanded = modelMenuExpanded, onDismissRequest = { modelMenuExpanded = false }) {
                    if (uiState.isLoadingModels) {
                        DropdownMenuItem(text = { Text("Cargando...") }, onClick = {}, enabled = false)
                    } else if (uiState.availableModels.isEmpty()){
                        DropdownMenuItem(text = { Text("No hay modelos para esta marca") }, onClick = {}, enabled = false)
                    }
                    else {
                        uiState.availableModels.forEach { model ->
                            DropdownMenuItem(text = { Text(model) }, onClick = { viewModel.onModelSelected(model); modelMenuExpanded = false })
                        }
                    }
                }
            }
            if (uiState.modelLoadError != null) Text(uiState.modelLoadError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Otros campos: BodyType, FuelType, Year, Version, CarColor, Price, Mileage, Description
        // Implementa estos de forma similar a UploadCarScreen, usando los valores de uiState
        // y llamando a los métodos onValueChanged del ViewModel.

        OutlinedTextField(value = uiState.version, onValueChange = viewModel::onVersionChanged, label = { Text("Versión*") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        // ... (Año - Desplegable) ...
        ExposedDropdownRow(
            label = "Año*",
            options = uiState.availableYears,
            selectedOption = uiState.year,
            onOptionSelected = viewModel::onYearSelected,
            enabled = true // Siempre habilitado
        )

        // ... (Tipo de Carrocería - Desplegable) ...
        ExposedDropdownRow(
            label = "Tipo de Carrocería*",
            options = uiState.availableBodyTypes,
            selectedOption = uiState.bodyType,
            onOptionSelected = viewModel::onBodyTypeChanged,
            enabled = true
        )
        // ... (Tipo de Combustible - Desplegable) ...
        ExposedDropdownRow(
            label = "Tipo de Combustible*",
            options = uiState.availableFuelTypes,
            selectedOption = uiState.fuelType,
            onOptionSelected = viewModel::onFuelTypeSelected,
            enabled = true
        )
        // ... (Color del Coche - Desplegable con muestra visual) ...
        CarColorDropdown(
            label = "Color del Vehículo*",
            selectedCarColor = uiState.selectedCarColor,
            availableColors = uiState.availableCarColors,
            onColorSelected = viewModel::onCarColorSelected,
            enabled = true
        )

        OutlinedTextField(value = uiState.price, onValueChange = viewModel::onPriceChanged, label = { Text("Precio (€)*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = uiState.mileage, onValueChange = viewModel::onMileageChanged, label = { Text("Kilometraje (km)*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = uiState.description, onValueChange = viewModel::onDescriptionChanged, label = { Text("Descripción*") }, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp), maxLines = 8)


        // Sección de Localización
        Text("Ubicación del Vehículo*", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
        OutlinedTextField(
            value = uiState.manualLocationInput,
            onValueChange = viewModel::onManualLocationInputChanged,
            label = { Text("Código Postal o Ciudad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = !uiState.isManualLocationValid,
            supportingText = { if (!uiState.isManualLocationValid) Text(uiState.locationValidationMessage ?: "Ubicación no válida", color = MaterialTheme.colorScheme.error) },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.searchManualLocation(); focusManager.clearFocus() }),
            trailingIcon = {
                IconButton(onClick = { viewModel.searchManualLocation(); focusManager.clearFocus() }, enabled = uiState.manualLocationInput.isNotBlank() && !uiState.isFetchingLocationDetails) {
                    Icon(Icons.Filled.Search, "Buscar ubicación")
                }
            },
            enabled = !uiState.isFetchingLocationDetails
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                focusManager.clearFocus()
                when (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    PackageManager.PERMISSION_GRANTED -> viewModel.onLocationPermissionGranted() // Llama al método del VM
                    else -> viewModel.triggerLocationPermissionRequest() // Llama al método del VM
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isFetchingLocationDetails
        ) {
            Icon(Icons.Filled.LocationOn, "Obtener ubicación actual")
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Usar mi ubicación actual")
        }
        if (uiState.isFetchingLocationDetails) { /* ... CircularProgressIndicator ... */ }
        if (uiState.finalCiudad != null || uiState.finalComunidadAutonoma != null) {
            Text("Detectado: ${uiState.finalCiudad ?: ""}, ${uiState.finalComunidadAutonoma ?: ""}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
        if (uiState.locationGeneralErrorMessage != null) Text(uiState.locationGeneralErrorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)


        // Botón Guardar Cambios
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.saveCarChanges() },
            enabled = !uiState.isSavingCar && !uiState.isUploadingImages && uiState.canEditCar,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (uiState.isSavingCar) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Guardar Cambios", fontSize = 16.sp) // Reemplazar con stringResource
            }
        }
    }
}

@Composable
fun ExistingImageItem(
    imageUrl: String,
    isMarkedForDeletion: Boolean,
    onToggleDeletion: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggleDeletion)
            .border(
                2.dp,
                if (isMarkedForDeletion) MaterialTheme.colorScheme.error else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build()
            ),
            contentDescription = "Imagen existente",
            modifier = Modifier.fillMaxSize().then(if(isMarkedForDeletion) Modifier.alpha(0.5f) else Modifier),
            contentScale = ContentScale.Crop
        )
        if (isMarkedForDeletion) {
            Icon(
                imageVector = Icons.Filled.CheckCircle, // O un icono de "basura"
                contentDescription = "Marcada para eliminar",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.7f), CircleShape)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun NewImageItem(uri: Uri, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = "Nueva imagen seleccionada",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                .size(24.dp)
        ) {
            Icon(
                Icons.Filled.Close,
                "Quitar imagen",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownRow(
    label: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean,
    isError: Boolean = false // Para validación visual
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if(enabled) expanded = !expanded }) {
            OutlinedTextField(
                value = selectedOption ?: "Seleccionar...",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                enabled = enabled,
                isError = isError,
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { onOptionSelected(option); expanded = false })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarColorDropdown( // Específico para CarColor con muestra visual
    label: String,
    availableColors: List<CarColor>,
    selectedCarColor: CarColor?,
    onColorSelected: (CarColor) -> Unit,
    enabled: Boolean,
    isError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if(enabled) expanded = !expanded }) {
            OutlinedTextField(
                value = selectedCarColor?.let { stringResource(id = it.displayNameResId) } ?: "Seleccionar...",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                leadingIcon = selectedCarColor?.let { { Box(Modifier.size(20.dp).background(it.colorValue, CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)) } },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                enabled = enabled,
                isError = isError,
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
                availableColors.forEach { colorOption ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(20.dp).background(colorOption.colorValue, CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape))
                                Spacer(Modifier.width(12.dp))
                                Text(text = stringResource(id = colorOption.displayNameResId))
                            }
                        },
                        onClick = { onColorSelected(colorOption); expanded = false }
                    )
                }
            }
        }
    }
}

