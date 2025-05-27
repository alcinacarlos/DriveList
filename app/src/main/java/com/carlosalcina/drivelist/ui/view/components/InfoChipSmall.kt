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
import androidx.compose.ui.unit.sp

@Composable
fun InfoChipSmall(text: String, modifier: Modifier = Modifier) {
    if (text.isNotBlank()) {
        Box(
            modifier = modifier
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}