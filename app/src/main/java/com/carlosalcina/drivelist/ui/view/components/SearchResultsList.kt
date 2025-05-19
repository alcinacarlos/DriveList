package com.carlosalcina.drivelist.ui.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlosalcina.drivelist.domain.model.CarForSale

@Composable
fun SearchResultsList(
    cars: List<CarForSale>,
    listState: LazyListState,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    isUserAuthenticated: Boolean,
    isTogglingFavoriteMap: Map<String, Boolean>,
    onCarClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (cars.isNotEmpty() && canLoadMore && !isLoadingMore) {
        // Comprobar si el último item visible está cerca del final de la lista
        val layoutInfo = listState.layoutInfo
        val visibleItemsInfo = layoutInfo.visibleItemsInfo
        if (visibleItemsInfo.isNotEmpty()) {
            val lastVisibleItemIndex = visibleItemsInfo.last().index
            if (lastVisibleItemIndex >= cars.size - 1 - 5) { // Cargar más cuando quedan 5 items o menos
                LaunchedEffect(lastVisibleItemIndex) { // Usar una clave que cambie
                    onLoadMore()
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cars, key = { "car_${it.id}" }) { car ->
            CarSearchCard( // Reutilizar la tarjeta de coche de HomeScreen
                car = car, // El ViewModel ya debería haber actualizado isFavoriteByCurrentUser
                isUserAuthenticated = isUserAuthenticated,
                isTogglingFavorite = isTogglingFavoriteMap[car.id] ?: false, // Estado de carga para este coche
                onClick = { onCarClick(car.id) },
                onToggleFavorite = { onToggleFavorite(car.id) }
            )
        }

        if (isLoadingMore) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

    }
}