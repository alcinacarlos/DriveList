package com.carlosalcina.drivelist.ui.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.navigation.Screen
import com.carlosalcina.drivelist.ui.view.components.AppBottomNavigationBar
import com.carlosalcina.drivelist.ui.view.components.BrandModelFilterDialog
import com.carlosalcina.drivelist.ui.view.components.LatestCarsSection
import com.carlosalcina.drivelist.ui.view.components.SearchFilterCard
import com.carlosalcina.drivelist.ui.view.components.TopBar
import com.carlosalcina.drivelist.ui.viewmodel.HomeScreenViewModel

//PANTALLA PRINCIPAL
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(), navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Efecto para mostrar errores de favoritos
    LaunchedEffect(uiState.favoriteToggleError) {
        uiState.favoriteToggleError?.let {
            snackbarHostState.showSnackbar(
                message = it, duration = SnackbarDuration.Short
            )
            viewModel.clearFavoriteToggleError()
        }
    }
    LaunchedEffect(uiState.carLoadError) {
        uiState.carLoadError?.let {
            snackbarHostState.showSnackbar(
                message = "Error cargando coches: $it", duration = SnackbarDuration.Long
            )
        }
    }
    LaunchedEffect(uiState.searchError) {
        uiState.searchError?.let {
            snackbarHostState.showSnackbar(
                message = "Error en búsqueda: $it", duration = SnackbarDuration.Long
            )
        }
    }


    Scaffold(
        topBar = {
            TopBar(navController, stringResource = R.string.screen_title_home)
        },
        bottomBar = {
            AppBottomNavigationBar(navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoadingLatestCars,
            onRefresh = { viewModel.onRefreshTriggered() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                SearchFilterCard(
                    filters = uiState.filters,
                    fuelTypeOptions = viewModel.fuelTypesForFilter,
                    onBrandModelClick = { viewModel.openBrandModelDialog() },
                    onMaxPriceChange = { viewModel.onMaxPriceChanged(it) },
                    onFuelTypeSelect = { viewModel.onFuelTypeSelected(it) },
                    onSearchClick = {
                        val currentFiltersFromHome = viewModel.uiState.value.filters
                        val route =
                            Screen.SearchVehicle.createRoute(filters = currentFiltersFromHome)
                        navController.navigate(route)
                    },
                    onClearBrandModel = { viewModel.clearBrandModelFilter() })

                // Mostrar resultados de búsqueda si existen, sino los últimos coches
                val carsToShow = if (uiState.searchedCars.isNotEmpty() || uiState.noSearchResults) {
                    uiState.searchedCars
                } else {
                    uiState.latestCars
                }
                val isLoading = if (uiState.searchedCars.isNotEmpty() || uiState.noSearchResults) {
                    uiState.isLoadingSearchedCars
                } else {
                    uiState.isLoadingLatestCars
                }
                val errorToShow =
                    if (uiState.searchedCars.isNotEmpty() || uiState.noSearchResults) {
                        uiState.searchError
                    } else {
                        uiState.carLoadError
                    }
                val sectionTitle =
                    if (uiState.searchedCars.isNotEmpty() || uiState.noSearchResults) {
                        if (uiState.noSearchResults) "No se encontraron resultados" else "Resultados de Búsqueda"
                    } else {
                        "Últimos Coches Publicados"
                    }


                LatestCarsSection(
                    title = sectionTitle,
                    isLoading = isLoading,
                    cars = carsToShow,
                    error = errorToShow,
                    favoriteCarIds = uiState.favoriteCarIds,
                    isUserAuthenticated = uiState.isUserAuthenticated,
                    onCarClick = { carId ->
                        navController.navigate(Screen.CarDetail.createRoute(carId))
                    },
                    onToggleFavorite = { carId ->
                        viewModel.toggleFavoriteStatus(carId)
                    },
                    onSeeMoreClick = {
                        val route =
                            Screen.SearchVehicle.createRoute(searchTerm = "coches_recientes")
                        navController.navigate(route)
                    },
                    showSeeMoreButton = !(uiState.searchedCars.isNotEmpty() || uiState.noSearchResults) // Solo para "últimos coches"
                )
            }
        }


        if (uiState.showBrandModelDialog) {
            BrandModelFilterDialog(
                brands = uiState.brands,
                models = uiState.models,
                initialSelectedBrand = uiState.selectedBrandForDialog, // <-- Cambio de nombre
                isLoadingBrands = uiState.isLoadingBrands,
                isLoadingModels = uiState.isLoadingModels,
                brandLoadError = uiState.brandLoadError,
                modelLoadError = uiState.modelLoadError,
                onBrandSelectedForModelFetch = { brand -> // <-- Llama a la misma función de antes
                    viewModel.onBrandSelectedInDialog(brand)
                },
                onFilterApplied = { brand, model -> // <-- Nuevo callback
                    viewModel.applyBrandModelFilter(brand, model)
                },
                onDismiss = { viewModel.closeBrandModelDialog() }
            )
        }
    }
}