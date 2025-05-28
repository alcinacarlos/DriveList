package com.carlosalcina.drivelist.ui.view.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carlosalcina.drivelist.navigation.Screen
import com.carlosalcina.drivelist.ui.view.components.SearchResultsList
import com.carlosalcina.drivelist.ui.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController, viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.refreshFavorites()
    }
    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.refreshFavorites() },
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading && uiState.favoriteCars.isEmpty() && uiState.error == null -> {
            }

            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            !uiState.isAuthenticated && !uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Debes iniciar sesión para ver tus favoritos.",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            uiState.favoriteCars.isEmpty() && !uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No tienes coches guardados en favoritos.",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                SearchResultsList(
                    cars = uiState.favoriteCars,
                    listState = lazyListState,
                    isLoadingMore = false,
                    canLoadMore = false,
                    isUserAuthenticated = uiState.isAuthenticated,
                    isTogglingFavoriteMap = uiState.isTogglingFavorite,
                    onCarClick = { carId ->
                        navController.navigate(Screen.CarDetail.createRoute(carId))
                    },
                    onToggleFavorite = { carId ->
                        viewModel.toggleFavoriteStatus(carId)
                    },
                    onLoadMore = {  },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
