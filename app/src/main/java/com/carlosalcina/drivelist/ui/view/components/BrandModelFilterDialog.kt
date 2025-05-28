package com.carlosalcina.drivelist.ui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/*DIÁLOGO DE FILTRO MARCA/MODELO */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandModelFilterDialog(
    brands: List<String>,
    models: List<String>,
    selectedBrand: String?,
    isLoadingBrands: Boolean,
    isLoadingModels: Boolean,
    brandLoadError: String?,
    modelLoadError: String?,
    onBrandSelected: (String) -> Unit,
    onModelSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var modelMenuExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Seleccionar Marca y Modelo") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Cerrar")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.Companion
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface) // Fondo del diálogo
            ) {
                // Sección de Marcas
                Text(
                    "Selecciona una Marca",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.Companion.padding(16.dp)
                )
                if (isLoadingBrands) {
                    CircularProgressIndicator(
                        modifier = Modifier.Companion
                            .align(Alignment.Companion.CenterHorizontally)
                            .padding(16.dp)
                    )
                } else if (brandLoadError != null) {
                    Text(
                        "Error cargando marcas: $brandLoadError",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.Companion.padding(16.dp)
                    )
                } else if (brands.isEmpty()) {
                    Text("No hay marcas disponibles.", modifier = Modifier.Companion.padding(16.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier.Companion.weight(1f), // Ocupa espacio disponible para marcas
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(brands) { brand ->
                            ListItem(
                                headlineContent = { Text(brand) },
                                modifier = Modifier.Companion
                                    .fillMaxWidth()
                                    .clickable { onBrandSelected(brand) }
                                    .background(
                                        if (brand == selectedBrand) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Companion.Transparent
                                    )
                                    .padding(vertical = 8.dp) // Padding vertical para cada item
                            )
                            HorizontalDivider()
                        }
                    }
                }

                // Sección de Modelos (si se ha seleccionado una marca)
                if (selectedBrand != null) {
                    HorizontalDivider(
                        thickness = 4.dp,
                        modifier = Modifier.Companion.padding(vertical = 8.dp)
                    )
                    Text(
                        "Selecciona un Modelo para $selectedBrand",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.Companion.padding(16.dp)
                    )
                    if (isLoadingModels) {
                        CircularProgressIndicator(
                            modifier = Modifier.Companion
                                .align(Alignment.Companion.CenterHorizontally)
                                .padding(16.dp)
                        )
                    } else if (modelLoadError != null) {
                        Text(
                            "Error cargando modelos: $modelLoadError",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.Companion.padding(16.dp)
                        )
                    } else if (models.isEmpty() && !isLoadingModels) { // Asegurar que no está cargando
                        Text(
                            "No hay modelos disponibles para $selectedBrand.",
                            modifier = Modifier.Companion.padding(16.dp)
                        )
                    } else if (models.isNotEmpty()) {
                        // Usar un Dropdown aquí puede ser un poco extraño en un diálogo de pantalla completa
                        // Considera otra LazyColumn para modelos si la lista es larga.
                        // Por ahora, mantendremos el ExposedDropdownMenuBox como lo pediste.
                        Box(
                            modifier = Modifier.Companion.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                        ) {
                            ExposedDropdownMenuBox(
                                expanded = modelMenuExpanded,
                                onExpandedChange = { modelMenuExpanded = !modelMenuExpanded },
                                modifier = Modifier.Companion.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = "Seleccionar modelo...", // Placeholder
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Modelo") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = modelMenuExpanded
                                        )
                                    },
                                    modifier = Modifier.Companion
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = modelMenuExpanded,
                                    onDismissRequest = { modelMenuExpanded = false },
                                    modifier = Modifier.Companion.fillMaxWidth()
                                ) {
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
                }
            }
        }
    }
}