package com.carlosalcina.drivelist.ui.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.data.preferences.LanguageRepository
import com.carlosalcina.drivelist.data.preferences.ThemeRepository
import com.carlosalcina.drivelist.localization.availableLanguages
import com.carlosalcina.drivelist.ui.theme.ThemeOption
import com.carlosalcina.drivelist.ui.view.components.TopBar
import com.carlosalcina.drivelist.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLanguageChange: (String) -> Unit,
    themeRepository: ThemeRepository,
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentLanguageCode by LanguageRepository.language.collectAsState(initial = LanguageRepository.DEFAULT_LANGUAGE)
    var languageMenuExpanded by remember { mutableStateOf(false) }
    val currentSelectedLanguage = availableLanguages.find { it.code == currentLanguageCode }
        ?: availableLanguages.first { it.code == LanguageRepository.DEFAULT_LANGUAGE }

    val currentTheme by themeRepository.theme.collectAsState(initial = ThemeOption.SYSTEM_DEFAULT)

    val passwordResetState by viewModel.passwordResetState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(passwordResetState.successMessage, passwordResetState.errorMessage) {
        val message = passwordResetState.successMessage ?: passwordResetState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopBar(navController, showBackArrow = true, stringResource = R.string.settings_title)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de Apariencia
            SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ThemeOption.entries.forEachIndexed { index, themeOption ->
                        val cornerRadius = 50
                        val shape = when (index) {
                            0 -> RoundedCornerShape(
                                topStartPercent = cornerRadius,
                                bottomStartPercent = cornerRadius
                            )
                            ThemeOption.entries.size - 1 -> RoundedCornerShape(
                                topEndPercent = cornerRadius,
                                bottomEndPercent = cornerRadius
                            )
                            else -> RectangleShape
                        }

                        SegmentedButton(
                            shape = shape,
                            onClick = { viewModel.onThemeChange(themeOption) },
                            selected = currentTheme == themeOption,
                            icon = {
                                SegmentedButtonDefaults.Icon(active = currentTheme == themeOption) {
                                    Icon(
                                        imageVector = when (themeOption) {
                                            ThemeOption.LIGHT -> Icons.Default.LightMode
                                            ThemeOption.DARK -> Icons.Default.DarkMode
                                            ThemeOption.SYSTEM_DEFAULT -> Icons.Default.Tonality
                                        },
                                        tint = MaterialTheme.colorScheme.inverseOnSurface,
                                        contentDescription = stringResource(id = themeOption.displayNameResId)
                                    )
                                }
                            }
                        ) {
                            Text(stringResource(id = themeOption.displayNameResId))
                        }
                    }
                }
            }


            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            SettingsSection(title = stringResource(R.string.settings_section_language)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = "Idioma",
                        modifier = Modifier.padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    ExposedDropdownMenuBox(
                        expanded = languageMenuExpanded,
                        onExpandedChange = { languageMenuExpanded = !languageMenuExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = stringResource(id = currentSelectedLanguage.displayNameResId),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageMenuExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false }
                        ) {
                            availableLanguages.forEach { languageItem ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = languageItem.displayNameResId)) },
                                    onClick = {
                                        if (currentLanguageCode != languageItem.code) {
                                            onLanguageChange(languageItem.code)
                                        }
                                        languageMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            SettingsSection(title = stringResource(R.string.settings_section_account)) {
                Button(
                    onClick = { viewModel.sendPasswordResetEmail() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !passwordResetState.isLoading
                ) {
                    if (passwordResetState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = stringResource(R.string.settings_change_password),
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.settings_change_password))
                    }
                }
                Text(
                    stringResource(R.string.settings_change_password_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}