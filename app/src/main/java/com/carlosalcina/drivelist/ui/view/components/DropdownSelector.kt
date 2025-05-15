package com.carlosalcina.drivelist.ui.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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