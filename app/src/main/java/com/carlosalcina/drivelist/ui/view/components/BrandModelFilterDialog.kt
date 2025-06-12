package com.carlosalcina.drivelist.ui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Diálogo de filtro que permite al usuario seleccionar una marca,
 * o una marca y un modelo.
 *
 * @param brands Lista de nombres de marcas a mostrar.
 * @param models Lista de nombres de modelos para la marca seleccionada.
 * @param initialSelectedBrand La marca que estaba seleccionada previamente, para mantener el estado.
 * @param isLoadingBrands Indica si las marcas se están cargando.
 * @param isLoadingModels Indica si los modelos se están cargando.
 * @param brandLoadError Mensaje de error si falla la carga de marcas.
 * @param modelLoadError Mensaje de error si falla la carga de modelos.
 * @param onBrandSelectedForModelFetch Callback que se ejecuta al seleccionar una marca para
 * que el ViewModel pueda cargar los modelos correspondientes.
 * @param onFilterApplied Callback que se ejecuta cuando el usuario confirma su selección.
 * Retorna la `brand` y el `model` (que puede ser null si solo se eligió la marca).
 * @param onDismiss Callback para cerrar el diálogo sin aplicar cambios.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandModelFilterDialog(
    brands: List<String>,
    models: List<String>,
    initialSelectedBrand: String?,
    isLoadingBrands: Boolean,
    isLoadingModels: Boolean,
    brandLoadError: String?,
    modelLoadError: String?,
    onBrandSelectedForModelFetch: (String) -> Unit,
    onFilterApplied: (brand: String, model: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var currentSelectedBrand by remember { mutableStateOf(initialSelectedBrand) }
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
                    title = { Text("Filtrar por Marca / Modelo", fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "Cerrar",
                                tint = MaterialTheme.colorScheme.inverseOnSurface
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                currentSelectedBrand?.let {
                                    onFilterApplied(it, null)
                                }
                            },
                            enabled = currentSelectedBrand != null
                        ) {
                            Text("APLICAR")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // --- Sección de Marcas ---
                Text(
                    "1. Selecciona una Marca",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                when {
                    isLoadingBrands -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp)
                        )
                    }

                    brandLoadError != null -> {
                        Text(
                            "Error cargando marcas: $brandLoadError",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    brands.isEmpty() -> {
                        Text("No hay marcas disponibles.", modifier = Modifier.padding(16.dp))
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f), // Ocupa el espacio disponible
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(brands) { brand ->
                                ListItem(
                                    headlineContent = { Text(brand) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Actualiza la marca seleccionada y pide los modelos
                                            currentSelectedBrand = brand
                                            onBrandSelectedForModelFetch(brand)
                                        }
                                        .background(
                                            if (brand == currentSelectedBrand) MaterialTheme.colorScheme.primaryContainer
                                            else Color.Transparent
                                        )
                                        .padding(vertical = 8.dp)
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }

                // --- Sección de Modelos (si se ha seleccionado una marca) ---
                if (currentSelectedBrand != null) {
                    HorizontalDivider(
                        thickness = 4.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        "2. Selecciona un Modelo (Opcional)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    when {
                        isLoadingModels -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(16.dp)
                            )
                        }

                        modelLoadError != null -> {
                            Text(
                                "Error cargando modelos: $modelLoadError",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        models.isEmpty() && !isLoadingModels -> {
                            Text(
                                "No hay modelos disponibles para $currentSelectedBrand.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        models.isNotEmpty() -> {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = modelMenuExpanded,
                                    onExpandedChange = { modelMenuExpanded = !modelMenuExpanded },
                                    modifier = Modifier.width(280.dp)
                                ) {
                                    OutlinedTextField(
                                        value = "Seleccionar modelo...",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Modelo para $currentSelectedBrand") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = modelMenuExpanded
                                            )
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .width(280.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = modelMenuExpanded,
                                        onDismissRequest = { modelMenuExpanded = false },
                                        modifier = Modifier.width(280.dp)
                                    ) {
                                        models.forEach { model ->
                                            DropdownMenuItem(
                                                text = { Text(model) },
                                                onClick = {
                                                    // Aplica filtro con marca y modelo
                                                    onFilterApplied(currentSelectedBrand!!, model)
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
}