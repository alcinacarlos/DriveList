package com.carlosalcina.drivelist.ui.view.screens

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.ui.view.components.AppBottomNavigationBar
import com.carlosalcina.drivelist.ui.view.components.DropdownSelector
import com.carlosalcina.drivelist.ui.view.components.TopBar
import com.carlosalcina.drivelist.ui.viewmodel.UploadCarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadCarScreen(
    viewModel: UploadCarViewModel = hiltViewModel(),
    onUploadSuccess: () -> Unit,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.onLocationPermissionGranted()
            } else {
                viewModel.onLocationPermissionDenied()
            }
        }
    )

    LaunchedEffect(uiState.isRequestingLocationPermission) {
        if (uiState.isRequestingLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    LaunchedEffect(uiState.locationGeneralErrorMessage) {
        uiState.locationGeneralErrorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            // viewModel.clearLocationError() // Necesitarías esta función en el ViewModel
        }
    }
    // Para mostrar mensajes de error o éxito
    LaunchedEffect(uiState.imageUploadErrorMessage) {
        uiState.imageUploadErrorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.resetFormUploadStatus() // Limpia el mensaje
        }
    }
    LaunchedEffect(uiState.generalErrorMessage) {
        uiState.generalErrorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.resetFormUploadStatus()
        }
    }
    LaunchedEffect(uiState.formUploadSuccess) {
        if (uiState.formUploadSuccess) {
            snackbarHostState.showSnackbar(
                message = "¡Coche subido con éxito!",
                duration = SnackbarDuration.Long
            )
            onUploadSuccess
        }
    }


    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
        onResult = { uris -> viewModel.onImagesSelected(uris) }
    )

    Scaffold(
        topBar = {
            TopBar(navController, stringResource = R.string.screen_title_sell)
        },
        bottomBar = {
            AppBottomNavigationBar(navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(horizontal = 18.dp)
            ,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //Sección de Selección de Imágenes
            Spacer(modifier = Modifier.height(10.dp))
            Text("Imágenes del Vehículo", style = MaterialTheme.typography.titleMedium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(uiState.selectedImageUris) { index, uri ->
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current).data(uri).build()
                            ),
                            contentDescription = "Imagen seleccionada ${index + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.removeSelectedImage(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .size(24.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                "Quitar imagen",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
                // Botón para añadir más imágenes si no se ha alcanzado el límite
                if (uiState.selectedImageUris.size < UploadCarViewModel.MAX_IMAGES) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(MaterialTheme.shapes.small)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    MaterialTheme.shapes.small
                                )
                                .clickable {
                                    multiplePhotoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.AddPhotoAlternate,
                                contentDescription = "Añadir imágenes",
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            // Desplegable de Marcas
            DropdownSelector(
                label = stringResource(R.string.label_brand),
                options = uiState.brands,
                selectedOption = uiState.selectedBrand,
                onOptionSelected = { viewModel.onBrandSelected(it) },
                isLoading = uiState.isLoadingBrands,
                enabled = !uiState.formUploadInProgress // Deshabilita mientras se sube
            )

            // Desplegable de Modelos
            DropdownSelector(
                label = "Modelo",
                options = uiState.models,
                selectedOption = uiState.selectedModel,
                onOptionSelected = { viewModel.onModelSelected(it) },
                isLoading = uiState.isLoadingModels,
                enabled = uiState.selectedBrand != null && !uiState.formUploadInProgress
            )

            // Desplegable de Carrocerías
            DropdownSelector(
                label = "Carrocería",
                options = uiState.bodyTypes,
                selectedOption = uiState.selectedBodyType,
                onOptionSelected = { viewModel.onBodyTypeSelected(it) },
                isLoading = uiState.isLoadingBodyTypes,
                enabled = uiState.selectedModel != null && !uiState.formUploadInProgress
            )

            // Desplegable de Combustibles
            DropdownSelector(
                label = "Combustible",
                options = uiState.fuelTypes,
                selectedOption = uiState.selectedFuelType,
                onOptionSelected = { viewModel.onFuelTypeSelected(it) },
                isLoading = uiState.isLoadingFuelTypes,
                enabled = uiState.selectedBodyType != null && !uiState.formUploadInProgress
            )

            // Desplegable de Años
            DropdownSelector(
                label = "Año",
                options = uiState.years,
                selectedOption = uiState.selectedYear,
                onOptionSelected = { viewModel.onYearSelected(it) },
                isLoading = uiState.isLoadingYears,
                enabled = uiState.selectedFuelType != null && !uiState.formUploadInProgress
            )

            // Desplegable de Versiones
            DropdownSelector(
                label = "Versión",
                options = uiState.versions,
                selectedOption = uiState.selectedVersion,
                onOptionSelected = { viewModel.onVersionSelected(it) },
                isLoading = uiState.isLoadingVersions,
                enabled = uiState.selectedYear != null && !uiState.formUploadInProgress
            )

            var carColorMenuExpanded by remember { mutableStateOf(false) }

            Column {
                Text(
                    "Color del Vehículo",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = carColorMenuExpanded,
                    onExpandedChange = {
                        if (!uiState.formUploadInProgress && !uiState.isUploadingImages) {
                            carColorMenuExpanded = !carColorMenuExpanded
                        }
                    }
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        readOnly = true,
                        value = uiState.selectedCarColor?.let { stringResource(id = it.displayNameResId) }
                            ?: stringResource(R.string.label_select_a_color),
                        onValueChange = {},
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = carColorMenuExpanded) },
                        leadingIcon = uiState.selectedCarColor?.let { selectedColorEnum ->
                            {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            selectedColorEnum.colorValue,
                                            CircleShape
                                        ) // Usar colorValue
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            CircleShape
                                        )
                                )
                            }
                        },
                        enabled = !uiState.formUploadInProgress && !uiState.isUploadingImages
                    )
                    ExposedDropdownMenu(
                        expanded = carColorMenuExpanded,
                        onDismissRequest = { carColorMenuExpanded = false }
                    ) {
                        uiState.availableCarColors.forEach { colorEnum ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(colorEnum.colorValue, CircleShape)
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outlineVariant,
                                                    CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(text = stringResource(id = colorEnum.displayNameResId))
                                    }
                                },
                                onClick = {
                                    viewModel.onCarColorSelected(colorEnum)
                                    carColorMenuExpanded = false
                                }
                            )
                        }
                        if (uiState.availableCarColors.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay colores disponibles") },
                                onClick = {},
                                enabled = false
                            )
                        }
                    }
                }
            }

            // Campos de texto
            OutlinedTextField(
                value = uiState.price,
                onValueChange = { viewModel.onPriceChanged(it) },
                label = { Text("Precio (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.formUploadInProgress
            )
            OutlinedTextField(
                value = uiState.mileage,
                onValueChange = { viewModel.onMileageChanged(it) },
                label = { Text("Kilometraje (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.formUploadInProgress
            )

            // Sección de Localización
            Text(
                "Ubicación del Vehículo",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            OutlinedTextField(
                value = uiState.manualLocationInput,
                onValueChange = viewModel::onManualLocationInputChanged,
                label = { Text("Código Postal") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = !uiState.isManualLocationValid,
                supportingText = {
                    if (!uiState.isManualLocationValid && uiState.locationValidationMessage != null) {
                        Text(
                            uiState.locationValidationMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search // Cambia el botón Enter a Lupa/Buscar
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        viewModel.searchManualLocation()
                        focusManager.clearFocus() // Ocultar teclado
                    }
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            viewModel.searchManualLocation()
                            focusManager.clearFocus()
                        },
                        enabled = uiState.manualLocationInput.isNotBlank() && !uiState.isFetchingLocationDetails
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar ubicación manual")
                    }
                },
                enabled = !uiState.isFetchingLocationDetails && !uiState.formUploadInProgress && !uiState.isUploadingImages && uiState.finalPostalCode == null
            )

            OutlinedButton(
                onClick = {
                    focusManager.clearFocus() // Ocultar teclado antes de pedir permiso/localización
                    when (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )) {
                        PackageManager.PERMISSION_GRANTED -> viewModel.fetchCurrentLocationAndPopulateInput()
                        else -> viewModel.triggerLocationPermissionRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isFetchingLocationDetails && !uiState.formUploadInProgress && !uiState.isUploadingImages
            ) {
                if (uiState.isFetchingLocationDetails) {
                    CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buscando ubicación...")
                } else {
                    Icon(
                        Icons.Filled.MyLocation,
                        contentDescription = "Obtener ubicación actual",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Usar mi ubicación actual")
                }

            }

            // Fin Sección de Localización

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                maxLines = 5,
                enabled = !uiState.formUploadInProgress
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.prepareAndSubmitCar() }, // Llama a la nueva función
                enabled = !uiState.formUploadInProgress && !uiState.isUploadingImages && uiState.selectedVersion != null && uiState.finalPostalCode != null && uiState.description.isNotBlank() && uiState.price.isNotBlank() && uiState.mileage.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.formUploadInProgress || uiState.isUploadingImages) { // Muestra progreso si alguna subida está activa
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.isUploadingImages) "Subiendo Imágenes..." else "Guardando Coche...")
                } else {
                    Text("Subir Coche")
                }
            }
        }
    }
}