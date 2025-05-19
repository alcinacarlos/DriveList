package com.carlosalcina.drivelist.ui.view.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.domain.model.CarColor
import com.carlosalcina.drivelist.domain.model.CarForSale

@SuppressLint("DefaultLocale")
@Composable
fun CarSearchCard(
    car: CarForSale,
    isUserAuthenticated: Boolean,
    isTogglingFavorite: Boolean, // Nuevo para el indicador de carga
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current
    val carColorValue = remember(car.carColor) {
        CarColor.fromName(car.carColor)?.colorValue ?: Color.Transparent
    }

    Card(
        modifier = Modifier
            .width(280.dp) // O usa .fillMaxWidth() si es en una LazyColumn principal
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Image(
                    painter = if (car.imageUrls.isNotEmpty()) {
                        rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(car.imageUrls.first())
                                .crossfade(true)
                                .error(R.drawable.no_photo)
                                .placeholder(R.drawable.no_photo)
                                .build()
                        )
                    } else {
                        painterResource(id = R.drawable.no_photo)
                    },
                    contentDescription = "Imagen de ${car.brand} ${car.model}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (carColorValue != Color.Transparent) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .size(28.dp)
                            .background(carColorValue, CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    )
                }
                if (isUserAuthenticated) {
                    Box( // Box para el CircularProgressIndicator y el IconButton
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(40.dp) // Tamaño para acomodar el progress
                    ) {
                        if (isTogglingFavorite) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center).size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (car.isFavoriteByCurrentUser) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = if (car.isFavoriteByCurrentUser) "Quitar de favoritos" else "Añadir a favoritos",
                                    tint = if (car.isFavoriteByCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${car.brand} ${car.model}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = car.version,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${String.format("%,.0f", car.price).replace(",", ".")} €",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = car.year, style = MaterialTheme.typography.bodySmall)
                    Text("•", style = MaterialTheme.typography.bodySmall)
                    Text(text = car.fuelType, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                if (!car.ciudad.isNullOrBlank()) {
                    Text(
                        text = car.ciudad,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}