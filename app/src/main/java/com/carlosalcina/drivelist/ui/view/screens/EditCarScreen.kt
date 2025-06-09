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
import androidx.compose.foundation.lazy.items
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
import com.carlosalcina.drivelist.ui.viewmodel.EditCarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCarScreen(
    carId: String,
    viewModel: EditCarViewModel = hiltViewModel(),
    navController: NavController,
    onUpdateSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = carId) {
        viewModel.loadCarDetails(carId)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) viewModel.onLocationPermissionGranted() else viewModel.onLocationPermissionDenied()
        }
    )

    LaunchedEffect(uiState.isRequestingLocationPermission) {
        if (uiState.isRequestingLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    LaunchedEffect(uiState.generalErrorMessage, uiState.locationGeneralErrorMessage, uiState.imageUploadErrorMessage) {
        val message = uiState.generalErrorMessage ?: uiState.locationGeneralErrorMessage ?: uiState.imageUploadErrorMessage
        message?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long)
            viewModel.resetFormUploadStatus()
        }
    }
    LaunchedEffect(uiState.formUploadSuccess) {
        if (uiState.formUploadSuccess) {
            snackbarHostState.showSnackbar("¡Coche actualizado con éxito!", duration = SnackbarDuration.Long)
            onUpdateSuccess()
        }
    }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = EditCarViewModel.MAX_IMAGES),
        onResult = { uris -> viewModel.onImagesSelected(uris) }
    )

    Scaffold(
        topBar = { TopBar(navController, stringResource = R.string.screen_title_edit, true) },
        bottomBar = { AppBottomNavigationBar(navController = navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoadingCarDetails) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Imágenes del Vehículo", style = MaterialTheme.typography.titleMedium)

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.existingImageUrls) { url ->
                        ImageItem(model = url, contentDescription = "Imagen existente", onRemove = { viewModel.removeExistingImage(url) })
                    }
                    items(uiState.newSelectedImageUris) { uri ->
                        ImageItem(model = uri, contentDescription = "Imagen nueva", onRemove = { viewModel.removeNewSelectedImage(uri) })
                    }
                    val totalImages = uiState.existingImageUrls.size + uiState.newSelectedImageUris.size
                    if (totalImages < EditCarViewModel.MAX_IMAGES) {
                        item {
                            AddImageButton {
                                multiplePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        }
                    }
                }

                DropdownSelector(label = stringResource(R.string.label_brand), options = uiState.brands, selectedOption = uiState.selectedBrand, onOptionSelected = viewModel::onBrandSelected, isLoading = uiState.isLoadingBrands, enabled = !uiState.formUploadInProgress)
                DropdownSelector(label = "Modelo", options = uiState.models, selectedOption = uiState.selectedModel, onOptionSelected = viewModel::onModelSelected, isLoading = uiState.isLoadingModels, enabled = uiState.selectedBrand != null && !uiState.formUploadInProgress)
                DropdownSelector(label = "Carrocería", options = uiState.bodyTypes, selectedOption = uiState.selectedBodyType, onOptionSelected = viewModel::onBodyTypeSelected, isLoading = uiState.isLoadingBodyTypes, enabled = uiState.selectedModel != null && !uiState.formUploadInProgress)
                DropdownSelector(label = "Combustible", options = uiState.fuelTypes, selectedOption = uiState.selectedFuelType, onOptionSelected = viewModel::onFuelTypeSelected, isLoading = uiState.isLoadingFuelTypes, enabled = uiState.selectedBodyType != null && !uiState.formUploadInProgress)
                DropdownSelector(label = "Año", options = uiState.years, selectedOption = uiState.selectedYear, onOptionSelected = viewModel::onYearSelected, isLoading = uiState.isLoadingYears, enabled = uiState.selectedFuelType != null && !uiState.formUploadInProgress)
                DropdownSelector(label = "Versión", options = uiState.versions, selectedOption = uiState.selectedVersion, onOptionSelected = viewModel::onVersionSelected, isLoading = uiState.isLoadingVersions, enabled = uiState.selectedYear != null && !uiState.formUploadInProgress)

                var carColorMenuExpanded by remember { mutableStateOf(false) }

                Column {
                    Text("Color del Vehículo", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 4.dp))
                    ExposedDropdownMenuBox(expanded = carColorMenuExpanded, onExpandedChange = { if (!uiState.formUploadInProgress) carColorMenuExpanded = !carColorMenuExpanded }) {
                        OutlinedTextField(
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            readOnly = true,
                            value = uiState.selectedCarColor?.let { stringResource(id = it.displayNameResId) } ?: stringResource(R.string.label_select_a_color),
                            onValueChange = {},
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = carColorMenuExpanded) },
                            leadingIcon = uiState.selectedCarColor?.let { { Box(modifier = Modifier.size(20.dp).background(it.colorValue, CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)) } },
                            enabled = !uiState.formUploadInProgress
                        )
                        ExposedDropdownMenu(expanded = carColorMenuExpanded, onDismissRequest = { carColorMenuExpanded = false }) {
                            uiState.availableCarColors.forEach { colorEnum ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(20.dp).background(colorEnum.colorValue, CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(text = stringResource(id = colorEnum.displayNameResId))
                                        }
                                    },
                                    onClick = { viewModel.onCarColorSelected(colorEnum); carColorMenuExpanded = false }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(value = uiState.price, onValueChange = viewModel::onPriceChanged, label = { Text("Precio (€)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !uiState.formUploadInProgress)
                OutlinedTextField(value = uiState.mileage, onValueChange = viewModel::onMileageChanged, label = { Text("Kilometraje (km)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !uiState.formUploadInProgress)

                Text("Ubicación del Vehículo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))

                OutlinedTextField(
                    value = uiState.manualLocationInput,
                    onValueChange = viewModel::onManualLocationInputChanged,
                    label = { Text("Código Postal o Ciudad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !uiState.isManualLocationValid,
                    supportingText = { if (!uiState.isManualLocationValid) uiState.locationValidationMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.searchManualLocation(); focusManager.clearFocus() }),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.searchManualLocation(); focusManager.clearFocus() }, enabled = uiState.manualLocationInput.isNotBlank() && !uiState.isFetchingLocationDetails) {
                            Icon(Icons.Filled.Search, contentDescription = "Buscar ubicación")
                        }
                    },
                    enabled = !uiState.isFetchingLocationDetails && !uiState.formUploadInProgress
                )

                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        when (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            PackageManager.PERMISSION_GRANTED -> viewModel.fetchCurrentLocationAndPopulateInput()
                            else -> viewModel.triggerLocationPermissionRequest()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isFetchingLocationDetails && !uiState.formUploadInProgress
                ) {
                    if (uiState.isFetchingLocationDetails) {
                        CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buscando ubicación...")
                    } else {
                        Icon(Icons.Filled.MyLocation, contentDescription = "Obtener ubicación actual", modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Usar mi ubicación actual")
                    }
                }

                OutlinedTextField(value = uiState.description, onValueChange = viewModel::onDescriptionChanged, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), maxLines = 5, enabled = !uiState.formUploadInProgress)

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = viewModel::prepareAndUpdateCar,
                    enabled = !uiState.formUploadInProgress && !uiState.isLoadingCarDetails,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.formUploadInProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (uiState.isUploadingImages) "Subiendo Imágenes..." else "Guardando Cambios...")
                    } else {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageItem(model: Any, contentDescription: String, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Image(
            painter = rememberAsyncImagePainter(ImageRequest.Builder(LocalContext.current).data(model).crossfade(true).build()),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape).size(24.dp)
        ) {
            Icon(Icons.Filled.Close, "Quitar imagen", tint = Color.White, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun AddImageButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(MaterialTheme.shapes.small)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Añadir imágenes", modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
    }
}