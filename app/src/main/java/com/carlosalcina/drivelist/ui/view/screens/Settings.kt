package com.carlosalcina.drivelist.ui.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlosalcina.drivelist.localization.LocalizedContext
import com.carlosalcina.drivelist.R

@Composable
fun SettingsScreen(onLanguageChange: (String) -> Unit) {
    val context = LocalizedContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = context.getString(R.string.sign_in_google))

        Button(onClick = { onLanguageChange("es") }) {
            Text("Español")
        }

        Button(onClick = { onLanguageChange("en") }) {
            Text("Inglés")
        }
    }
}
