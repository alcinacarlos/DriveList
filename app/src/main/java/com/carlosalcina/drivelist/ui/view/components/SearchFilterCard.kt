package com.carlosalcina.drivelist.ui.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.carlosalcina.drivelist.domain.model.CarSearchFilters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterCard(
    filters: CarSearchFilters,
    fuelTypeOptions: List<String>,
    onBrandModelClick: () -> Unit,
    onMaxPriceChange: (String) -> Unit,
    onFuelTypeSelect: (String?) -> Unit,
    onSearchClick: () -> Unit,
    onClearBrandModel: () -> Unit
) {
    var fuelMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.Companion.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Encuentra tu próximo coche",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.Companion.align(Alignment.Companion.CenterHorizontally)
            )

            // Campo de Marca y Modelo (Clickable)
            OutlinedTextField(
                value = if (filters.brand != null && filters.model != null) "${filters.brand} - ${filters.model}"
                else filters.brand ?: "Todas las Marcas y Modelos",
                onValueChange = { },
                label = { Text("Marca y Modelo") },
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .clickable(onClick = onBrandModelClick),
                enabled = false, // Deshabilitado para que el click funcione en todo el área
                colors = TextFieldDefaults.colors( // Usar .colors() para M3
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = Color.Companion.Transparent, // Fondo transparente
                    disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary, // Icono con color primario
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                leadingIcon = {
                    Icon(
                        Icons.Filled.FilterList,
                        contentDescription = "Filtro Marca/Modelo"
                    )
                },
                trailingIcon = if (filters.brand != null || filters.model != null) {
                    {
                        IconButton(
                            onClick = onClearBrandModel,
                            modifier = Modifier.Companion.size(24.dp)
                        ) { // Tamaño del icono
                            Icon(Icons.Filled.Close, contentDescription = "Limpiar Marca/Modelo")
                        }
                    }
                } else null,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )

            Row(
                Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Campo de Precio Máximo
                OutlinedTextField(
                    value = filters.maxPrice?.toString() ?: "",
                    onValueChange = onMaxPriceChange,
                    label = { Text("Precio Máx (€)") },
                    placeholder = { Text("Ej: 20000") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Companion.Number,
                        imeAction = ImeAction.Companion.Next
                    ),
                    modifier = Modifier.Companion.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )

                // Desplegable de Tipo de Combustible
                Box(modifier = Modifier.Companion.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = fuelMenuExpanded,
                        onExpandedChange = { fuelMenuExpanded = !fuelMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = filters.fuelType ?: "Todos",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Combustible") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelMenuExpanded) },
                            modifier = Modifier.Companion
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = fuelMenuExpanded,
                            onDismissRequest = { fuelMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todos los combustibles") },
                                onClick = {
                                    onFuelTypeSelect(null)
                                    fuelMenuExpanded = false
                                }
                            )
                            fuelTypeOptions.forEach { fuel ->
                                DropdownMenuItem(
                                    text = { Text(fuel) },
                                    onClick = {
                                        onFuelTypeSelect(fuel)
                                        fuelMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }


            Button(
                onClick = onSearchClick,
                modifier = Modifier.Companion.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Search, contentDescription = "Buscar")
                Spacer(Modifier.Companion.size(ButtonDefaults.IconSpacing))
                Text("Buscar Coches")
            }
        }
    }
}