package com.carlosalcina.drivelist.ui.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.carlosalcina.drivelist.domain.model.CarSearchFilters
import java.util.Calendar

// --- DIÁLOGO DE FILTROS AVANZADOS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFiltersDialog(
    initialFilters: CarSearchFilters, // Para poblar los campos del diálogo
    brands: List<String>,
    models: List<String>,
    selectedBrandInDialog: String?,
    selectedModelInDialog: String?,
    minYearInput: String,
    maxPriceInput: String,
    locationInput: String,
    isLoadingBrands: Boolean,
    isLoadingModels: Boolean,
    onBrandSelected: (String) -> Unit,
    onModelSelected: (String) -> Unit,
    onMinYearChanged: (String) -> Unit,
    onMaxPriceChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    // Estado local para los desplegables dentro del diálogo
    var brandMenuExpanded by remember { mutableStateOf(false) }
    var modelMenuExpanded by remember { mutableStateOf(false) }


    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Filtros Avanzados") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Cerrar")
                        }
                    },
                    actions = {
                        TextButton(onClick = onClearFilters) {
                            Text("Limpiar")
                        }
                    }
                )
            },
            bottomBar = {
                Surface(shadowElevation = 8.dp) { // Sombra para el botón
                    Button(
                        onClick = onApplyFilters,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Aplicar Filtros", fontSize = 16.sp)
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Marca
                Text("Marca", style = MaterialTheme.typography.titleSmall)
                ExposedDropdownMenuBox(
                    expanded = brandMenuExpanded,
                    onExpandedChange = { brandMenuExpanded = !brandMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedBrandInDialog ?: "Seleccionar Marca...",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandMenuExpanded) },
                        leadingIcon = { Icon(Icons.Filled.FilterList, null)},
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = brandMenuExpanded,
                        onDismissRequest = { brandMenuExpanded = false }
                    ) {
                        if (isLoadingBrands) {
                            DropdownMenuItem(text = { Text("Cargando marcas...") }, onClick = {}, enabled = false)
                        } else {
                            brands.forEach { brand ->
                                DropdownMenuItem(
                                    text = { Text(brand) },
                                    onClick = {
                                        onBrandSelected(brand)
                                        brandMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Modelo (solo si se ha seleccionado marca)
                if (selectedBrandInDialog != null) {
                    Text("Modelo para $selectedBrandInDialog", style = MaterialTheme.typography.titleSmall)
                    ExposedDropdownMenuBox(
                        expanded = modelMenuExpanded,
                        onExpandedChange = { modelMenuExpanded = !modelMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedModelInDialog ?: "Seleccionar Modelo...",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelMenuExpanded) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = modelMenuExpanded,
                            onDismissRequest = { modelMenuExpanded = false }
                        ) {
                            if (isLoadingModels) {
                                DropdownMenuItem(text = { Text("Cargando modelos...") }, onClick = {}, enabled = false)
                            } else if (models.isEmpty()){
                                DropdownMenuItem(text = { Text("No hay modelos") }, onClick = {}, enabled = false)
                            }
                            else {
                                models.forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model) },
                                        onClick = {
                                            onModelSelected(model)
                                            modelMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Año Mínimo
                OutlinedTextField(
                    value = minYearInput,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && (newValue.isEmpty() || newValue.length <= 4)) {
                            onMinYearChanged(newValue)
                        }
                    },
                    label = { Text("Año Mínimo (Ej: ${currentYear - 5})") },
                    placeholder = { Text("Ej: 2018") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                // Precio Máximo
                OutlinedTextField(
                    value = maxPriceInput,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            onMaxPriceChanged(newValue)
                        }
                    },
                    label = { Text("Precio Máximo (€)") },
                    placeholder = { Text("Ej: 15000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                // Ubicación (Ciudad/Comunidad)
                OutlinedTextField(
                    value = locationInput,
                    onValueChange = onLocationChanged,
                    label = { Text("Ciudad o Comunidad Autónoma") },
                    placeholder = { Text("Ej: Madrid") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { /* Podrías ocultar teclado */ }),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}