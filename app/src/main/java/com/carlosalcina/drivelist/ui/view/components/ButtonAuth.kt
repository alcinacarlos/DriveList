package com.carlosalcina.drivelist.ui.view.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ButtonAuth(
    onClick: () -> Unit,
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    Button(
        onClick = { onClick() },
        modifier = Modifier.fillMaxWidth().height(55.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        enabled = enabled
    ){
        content()
    }
}