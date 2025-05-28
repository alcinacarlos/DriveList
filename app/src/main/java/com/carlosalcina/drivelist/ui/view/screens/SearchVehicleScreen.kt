package com.carlosalcina.drivelist.ui.view.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carlosalcina.drivelist.domain.model.QuickFilter
import com.carlosalcina.drivelist.navigation.Screen
import com.carlosalcina.drivelist.ui.view.components.AdvancedFiltersDialog
import com.carlosalcina.drivelist.ui.view.components.SearchResultsList
import com.carlosalcina.drivelist.ui.viewmodel.SearchVehicleScreenViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchVehicleScreen(
    viewModel: SearchVehicleScreenViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // Efectos para mostrar errores
    LaunchedEffect(uiState.searchError, uiState.favoriteToggleError) {
        val error = uiState.searchError ?: uiState.favoriteToggleError
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            if (uiState.favoriteToggleError != null) viewModel.clearFavoriteToggleError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            SearchBarWithFilterButton(
                searchTerm = uiState.currentSearchTerm,
                onSearchTermChange = { viewModel.onSearchTermChanged(it) },
                onSearchAction = { viewModel.onPerformSearchFromBar() },
                onFilterAction = { viewModel.openAdvancedFiltersDialog() }
            )
            QuickFiltersRow(
                quickFilters = uiState.quickFilters,
                activeQuickFilterIds = uiState.activeQuickFilterIds,
                onQuickFilterClicked = { viewModel.onApplyQuickFilter(it) }
            )

            if (uiState.isLoading && uiState.searchResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.noResultsFound && uiState.searchResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No se encontraron coches con los filtros actuales. Prueba a modificarlos.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                SearchResultsList(
                    cars = uiState.searchResults,
                    listState = listState,
                    isLoadingMore = uiState.isLoadingMore,
                    canLoadMore = uiState.canLoadMore,
                    isUserAuthenticated = uiState.isUserAuthenticated,
                    isTogglingFavoriteMap = uiState.isTogglingFavorite,
                    onCarClick = { carId ->
                        Log.d("SearchScreen", "Coche clickeado: $carId")
                        navController.navigate(Screen.CarDetail.createRoute(carId))
                    },
                    onToggleFavorite = { carId ->
                        viewModel.toggleFavoriteStatus(carId)
                    },
                    onLoadMore = {
                        viewModel.onLoadMoreResults()
                    }
                )
            }
        }

        if (uiState.showAdvancedFiltersDialog) {
            AdvancedFiltersDialog(
                initialFilters = uiState.tempAdvancedFilters, // Usar tempAdvancedFilters
                brands = uiState.brandsForDialog,
                models = uiState.modelsForDialog,
                selectedBrandInDialog = uiState.selectedBrandInDialog,
                selectedModelInDialog = uiState.selectedModelInDialog,
                minYearInput = uiState.advancedFilterMinYearInput,
                maxPriceInput = uiState.advancedFilterMaxPriceInput,
                locationInput = uiState.advancedFilterLocationInput,
                isLoadingBrands = uiState.isLoadingBrandsForDialog,
                isLoadingModels = uiState.isLoadingModelsForDialog,
                onBrandSelected = { viewModel.onBrandSelectedInDialog(it) },
                onModelSelected = { viewModel.onModelSelectedInDialog(it) },
                onMinYearChanged = { viewModel.onAdvancedFilterMinYearChanged(it) },
                onMaxPriceChanged = { viewModel.onAdvancedFilterMaxPriceChanged(it) },
                onLocationChanged = { viewModel.onAdvancedFilterLocationChanged(it) },
                onApplyFilters = { viewModel.applyAdvancedFilters() },
                onClearFilters = { viewModel.clearAdvancedFilters() },
                onDismiss = { viewModel.closeAdvancedFiltersDialog() }
            )
        }
    }
}

// --- BARRA DE BÚSQUEDA CON BOTÓN DE FILTRO ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarWithFilterButton(
    searchTerm: String,
    onSearchTermChange: (String) -> Unit,
    onSearchAction: () -> Unit,
    onFilterAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSearchAction) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BasicTextField(
                value = searchTerm,
                onValueChange = onSearchTermChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    onSearchAction()
                    focusManager.clearFocus()
                }),
                decorationBox = { innerTextField ->
                    if (searchTerm.isEmpty()) {
                        Text("Buscar por marca, modelo, versión...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    innerTextField()
                }
            )
            if (searchTerm.isNotEmpty()) {
                IconButton(onClick = { onSearchTermChange("") }) { // Limpiar texto
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = "Limpiar búsqueda",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onFilterAction) {
                Icon(
                    Icons.Filled.FilterList,
                    contentDescription = "Abrir filtros avanzados",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// --- FILA DE FILTROS RÁPIDOS ---
@Composable
fun QuickFiltersRow(
    quickFilters: List<QuickFilter>,
    activeQuickFilterIds: Set<String>,
    onQuickFilterClicked: (QuickFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(quickFilters, key = { it.id }) { filter ->
            FilterChip(
                selected = activeQuickFilterIds.contains(filter.id),
                onClick = { onQuickFilterClicked(filter) },
                label = { Text(filter.displayText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.inverseSurface
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}