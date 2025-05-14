package com.carlosalcina.drivelist.ui.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
    LaunchedEffect(uiState.generalErrorMessage) {
        uiState.generalErrorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.resetUploadStatus() // Limpia el mensaje para que no se muestre de nuevo
        }
    }

    LaunchedEffect(uiState.uploadSuccess) {
        if (uiState.uploadSuccess) {
            snackbarHostState.showSnackbar(
                message = "¡Coche subido con éxito!",
                duration = SnackbarDuration.Long
            )
            // Considera navegar hacia atrás o a "mis anuncios"
            // navController.popBackStack()
            viewModel.resetUploadStatus() // Resetea el estado para futuras subidas o reintentos
            // Podrías también limpiar el formulario aquí si lo deseas, llamando a una función en el ViewModel
        }
    }

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

            // Desplegable de Marcas
            DropdownSelector(
                label = "Marca",
                options = uiState.brands,
                selectedOption = uiState.selectedBrand,
                onOptionSelected = { viewModel.onBrandSelected(it) },
                isLoading = uiState.isLoadingBrands,
                enabled = !uiState.uploadInProgress // Deshabilita mientras se sube
            )

            // Desplegable de Modelos
            DropdownSelector(
                label = "Modelo",
                options = uiState.models,
                selectedOption = uiState.selectedModel,
                onOptionSelected = { viewModel.onModelSelected(it) },
                isLoading = uiState.isLoadingModels,
                enabled = uiState.selectedBrand != null && !uiState.uploadInProgress
            )

            // Desplegable de Carrocerías
            DropdownSelector(
                label = "Carrocería",
                options = uiState.bodyTypes,
                selectedOption = uiState.selectedBodyType,
                onOptionSelected = { viewModel.onBodyTypeSelected(it) },
                isLoading = uiState.isLoadingBodyTypes,
                enabled = uiState.selectedModel != null && !uiState.uploadInProgress
            )

            // Desplegable de Combustibles
            DropdownSelector(
                label = "Combustible",
                options = uiState.fuelTypes,
                selectedOption = uiState.selectedFuelType,
                onOptionSelected = { viewModel.onFuelTypeSelected(it) },
                isLoading = uiState.isLoadingFuelTypes,
                enabled = uiState.selectedBodyType != null && !uiState.uploadInProgress
            )

            // Desplegable de Años
            DropdownSelector(
                label = "Año",
                options = uiState.years,
                selectedOption = uiState.selectedYear,
                onOptionSelected = { viewModel.onYearSelected(it) },
                isLoading = uiState.isLoadingYears,
                enabled = uiState.selectedFuelType != null && !uiState.uploadInProgress
            )

            // Desplegable de Versiones
            DropdownSelector(
                label = "Versión",
                options = uiState.versions,
                selectedOption = uiState.selectedVersion,
                onOptionSelected = { viewModel.onVersionSelected(it) },
                isLoading = uiState.isLoadingVersions,
                enabled = uiState.selectedYear != null && !uiState.uploadInProgress
            )

            // Campos de texto
            OutlinedTextField(
                value = uiState.price,
                onValueChange = { viewModel.onPriceChanged(it) },
                label = { Text("Precio (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.uploadInProgress
            )
            OutlinedTextField(
                value = uiState.mileage,
                onValueChange = { viewModel.onMileageChanged(it) },
                label = { Text("Kilometraje (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.uploadInProgress
            )
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), // Para que sea un poco más alto
                maxLines = 5,
                enabled = !uiState.uploadInProgress
            )

            // TODO: Sección para subir imágenes

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.submitCar() },
                enabled = !uiState.uploadInProgress && uiState.selectedVersion != null, // Habilita solo si todos los desplegables están seleccionados y no se está subiendo
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.uploadInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subiendo...")
                } else {
                    Text("Subir Coche")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    isLoading: Boolean,
    enabled: Boolean = true // Para deshabilitar el desplegable entero
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded && enabled && !isLoading && options.isNotEmpty(), // Solo expandir si hay opciones y no está cargando
            onExpandedChange = { if (enabled && !isLoading && options.isNotEmpty()) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedOption ?: if (isLoading) "Cargando..." else "Selecciona...",
                onValueChange = {}, // No editable directamente
                readOnly = true,
                label = {  }, // El label ya está arriba
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled && options.isNotEmpty())
                    }
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                enabled = enabled // El TextField también se deshabilita
            )
            ExposedDropdownMenu(
                expanded = expanded && enabled && !isLoading && options.isNotEmpty(),
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
                if (options.isEmpty() && !isLoading) {
                    DropdownMenuItem(
                        text = { Text("No hay opciones disponibles") },
                        onClick = {},
                        enabled = false
                    )
                }
            }
        }
    }
}