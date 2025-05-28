package com.carlosalcina.drivelist.ui.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlosalcina.drivelist.domain.model.CarForSale

//SECCIÓN DE ÚLTIMOS COCHES
@Composable
fun LatestCarsSection(
    title: String,
    isLoading: Boolean,
    cars: List<CarForSale>,
    error: String?,
    favoriteCarIds: Set<String>,
    isUserAuthenticated: Boolean,
    onCarClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSeeMoreClick: () -> Unit,
    showSeeMoreButton: Boolean
) {
    Column(modifier = Modifier.Companion.padding(vertical = 8.dp)) { // Reducido padding vertical
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (showSeeMoreButton && cars.isNotEmpty()) { // Mostrar "Ver más" solo si hay coches y es la sección de "últimos"
                TextButton(onClick = onSeeMoreClick) {
                    Text("Ver más")
                }
            }
        }

        Spacer(modifier = Modifier.Companion.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Companion.Center
            ) { // Altura ajustada
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Text(
                "Error: $error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.Companion.padding(horizontal = 16.dp)
            )
        } else if (cars.isEmpty() && (title == "Últimos Coches Publicados" || title == "No se encontraron resultados")) {
            Text(
                if (title == "No se encontraron resultados") title else "No hay coches publicados recientemente.",
                modifier = Modifier.Companion
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Companion.CenterHorizontally),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cars, key = { it.id }) { car ->
                    CarItemCard(
                        car = car.copy(isFavoriteByCurrentUser = favoriteCarIds.contains(car.id)),
                        isUserAuthenticated = isUserAuthenticated,
                        onClick = { onCarClick(car.id) },
                        onToggleFavorite = { onToggleFavorite(car.id) }
                    )
                }
            }
        }
    }
}