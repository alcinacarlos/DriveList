package com.carlosalcina.drivelist.ui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Displaying small pieces of info (Year, Km, Fuel)
@Composable
fun InfoChip(text: String, modifier: Modifier = Modifier, maxLines: Int = 1) {
    if (text.isNotBlank()){
        Box(
            modifier = modifier
                .background(
                    MaterialTheme.colorScheme.inverseSurface,
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                maxLines = maxLines,
                overflow = if (maxLines == 1) TextOverflow.Companion.Ellipsis else TextOverflow.Companion.Clip
            )
        }
    }
}