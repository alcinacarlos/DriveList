package com.carlosalcina.drivelist.ui.view.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.carlosalcina.drivelist.ui.view.components.DropdownSelector
import com.carlosalcina.drivelist.ui.viewmodel.UploadCarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadCarScreen(
    viewModel: UploadCarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

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
            snackbarHostState.showSnackbar(message = "¡Coche subido con éxito!", duration = SnackbarDuration.Long)
            // navController.popBackStack() // O navega
            // ViewModel ya limpia el formulario y recarga marcas
        }
    }


    // Launcher para el selector de múltiples imágenes moderno
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
        onResult = { uris -> viewModel.onImagesSelected(uris) }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Subir Nuevo Coche") },
                // Puedes añadir un botón de navegación para ir atrás si es necesario
                // navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(18.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Sección de Selección de Imágenes ---
            Text("Imágenes del Vehículo", style = MaterialTheme.typography.titleMedium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(uiState.selectedImageUris) { index, uri ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
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
                            Icon(Icons.Filled.Close, "Quitar imagen", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                // Botón para añadir más imágenes si no se ha alcanzado el límite
                if (uiState.selectedImageUris.size < UploadCarViewModel.MAX_IMAGES) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
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
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            // Desplegable de Marcas
            DropdownSelector(
                label = "Marca",
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
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), // Para que sea un poco más alto
                maxLines = 5,
                enabled = !uiState.formUploadInProgress
            )

            // TODO: Sección para subir imágenes

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.prepareAndSubmitCar() }, // Llama a la nueva función
                enabled = !uiState.formUploadInProgress && !uiState.isUploadingImages && uiState.selectedVersion != null,
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