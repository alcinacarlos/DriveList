package com.carlosalcina.drivelist.ui.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.data.preferences.LanguageRepository
import com.carlosalcina.drivelist.ui.view.screens.settings.availableLanguages


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLanguageChange: (String) -> Unit,
    // Opcional: Si quieres un botón de atrás en la TopAppBar
    // onNavigateBack: (() -> Unit)? = null
) {
    val currentLanguageCode by LanguageRepository.language.collectAsState(initial = LanguageRepository.DEFAULT_LANGUAGE)
    var expanded by remember { mutableStateOf(false) }

    val currentSelectedLanguage = availableLanguages.find { it.code == currentLanguageCode }
        ?: availableLanguages.first { it.code == LanguageRepository.DEFAULT_LANGUAGE }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
                // navigationIcon = { if (onNavigateBack != null) IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.settings_language_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f) // Permite que el dropdown tome el espacio restante
                ) {
                    OutlinedTextField(
                        value = stringResource(id = currentSelectedLanguage.displayNameResId),
                        onValueChange = {}, // No se cambia directamente aquí
                        readOnly = true,
                        label = { Text(stringResource(id = R.string.settings_select_language)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor() // Importante para anclar el menú
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableLanguages.forEach { languageItem ->
                            DropdownMenuItem(
                                text = { Text(stringResource(id = languageItem.displayNameResId)) },
                                onClick = {
                                    if (currentLanguageCode != languageItem.code) {
                                        onLanguageChange(languageItem.code)
                                    }
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            // Aquí puedes añadir más ajustes si los necesitas
        }
    }
}