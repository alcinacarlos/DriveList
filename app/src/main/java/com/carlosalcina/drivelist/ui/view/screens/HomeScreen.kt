package com.carlosalcina.drivelist.ui.view.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.domain.model.CarColor
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.CarSearchFilters
import com.carlosalcina.drivelist.navigation.Screen
import com.carlosalcina.drivelist.ui.viewmodel.HomeScreenViewModel

// --- PANTALLA PRINCIPAL ---
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // navController: NavController, // Para navegar a "Ver Más" o detalles del coche
    viewModel: HomeScreenViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState() // Para el scroll vertical principal
    val snackbarHostState = remember { SnackbarHostState() }

    // Efecto para mostrar errores de favoritos
    LaunchedEffect(uiState.favoriteToggleError) {
        uiState.favoriteToggleError?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearFavoriteToggleError() // Limpiar el error después de mostrarlo
        }
    }
    LaunchedEffect(uiState.carLoadError) {
        uiState.carLoadError?.let {
            snackbarHostState.showSnackbar(
                message = "Error cargando coches: $it",
                duration = SnackbarDuration.Long
            )
        }
    }
    LaunchedEffect(uiState.searchError) {
        uiState.searchError?.let {
            snackbarHostState.showSnackbar(
                message = "Error en búsqueda: $it",
                duration = SnackbarDuration.Long
            )
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState) // Scroll para toda la pantalla si el contenido excede
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
                    val route = Screen.SearchVehicle.createRoute(filters = currentFiltersFromHome)
                    navController.navigate(route)
                },
                onClearBrandModel = { viewModel.clearBrandModelFilter() }
            )

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
            val errorToShow = if (uiState.searchedCars.isNotEmpty() || uiState.noSearchResults) {
                uiState.searchError
            } else {
                uiState.carLoadError
            }
            val sectionTitle = if (uiState.searchedCars.isNotEmpty() || uiState.noSearchResults) {
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
                    Log.d("HomeScreen", "Coche clickeado: $carId")
                    // navController.navigate("carDetail/$carId")
                },
                onToggleFavorite = { carId ->
                    viewModel.toggleFavoriteStatus(carId)
                },
                onSeeMoreClick = {
                    val route = Screen.SearchVehicle.createRoute(searchTerm = "coches_recientes")
                    navController.navigate(route)
                },
                showSeeMoreButton = !(uiState.searchedCars.isNotEmpty() || uiState.noSearchResults) // Solo para "últimos coches"
            )
        }

        if (uiState.showBrandModelDialog) {
            BrandModelFilterDialog(
                brands = uiState.brands,
                models = uiState.models,
                selectedBrand = uiState.selectedBrandForDialog,
                isLoadingBrands = uiState.isLoadingBrands,
                isLoadingModels = uiState.isLoadingModels,
                brandLoadError = uiState.brandLoadError,
                modelLoadError = uiState.modelLoadError,
                onBrandSelected = { viewModel.onBrandSelectedInDialog(it) },
                onModelSelected = { viewModel.onModelSelectedInDialog(it) },
                onDismiss = { viewModel.closeBrandModelDialog() }
            )
        }
    }
}

// --- TARJETA DE FILTROS DE BÚSQUEDA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterCard(
    filters: CarSearchFilters,
    fuelTypeOptions: List<String>,
    onBrandModelClick: () -> Unit,
    onMaxPriceChange: (String) -> Unit,
    onFuelTypeSelect: (String?) -> Unit,
    onSearchClick: () -> Unit,
    onClearBrandModel: () -> Unit
) {
    var fuelMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // Ajuste de padding
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp), // Bordes más redondeados,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Encuentra tu próximo coche", // Podría ser un stringResource
                style = MaterialTheme.typography.titleLarge, // Un poco más grande
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Campo de Marca y Modelo (Clickable)
            OutlinedTextField(
                value = if (filters.brand != null && filters.model != null) "${filters.brand} - ${filters.model}"
                else if (filters.brand != null) filters.brand
                else "Todas las Marcas y Modelos", // Placeholder mejorado
                onValueChange = { /* No editable directamente */ },
                label = { Text("Marca y Modelo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onBrandModelClick),
                enabled = false, // Deshabilitado para que el click funcione en todo el área
                colors = TextFieldDefaults.colors( // Usar .colors() para M3
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = Color.Transparent, // Fondo transparente
                    disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary, // Icono con color primario
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                leadingIcon = {
                    Icon(
                        Icons.Filled.FilterList,
                        contentDescription = "Filtro Marca/Modelo"
                    )
                },
                trailingIcon = if (filters.brand != null || filters.model != null) {
                    {
                        IconButton(
                            onClick = onClearBrandModel,
                            modifier = Modifier.size(24.dp)
                        ) { // Tamaño del icono
                            Icon(Icons.Filled.Close, contentDescription = "Limpiar Marca/Modelo")
                        }
                    }
                } else null,
                shape = RoundedCornerShape(8.dp)
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Campo de Precio Máximo
                OutlinedTextField(
                    value = filters.maxPrice?.toString() ?: "",
                    onValueChange = onMaxPriceChange,
                    label = { Text("Precio Máx (€)") },
                    placeholder = { Text("Ej: 20000") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )

                // Desplegable de Tipo de Combustible
                Box(modifier = Modifier.weight(1f)) { // Box para alinear el dropdown
                    ExposedDropdownMenuBox(
                        expanded = fuelMenuExpanded,
                        onExpandedChange = { fuelMenuExpanded = !fuelMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = filters.fuelType ?: "Todos",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Combustible") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelMenuExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = fuelMenuExpanded,
                            onDismissRequest = { fuelMenuExpanded = false }
                        ) {
                            DropdownMenuItem( // Opción para "Todos" o limpiar filtro
                                text = { Text("Todos los combustibles") },
                                onClick = {
                                    onFuelTypeSelect(null) // Pasar null para limpiar
                                    fuelMenuExpanded = false
                                }
                            )
                            fuelTypeOptions.forEach { fuel ->
                                DropdownMenuItem(
                                    text = { Text(fuel) },
                                    onClick = {
                                        onFuelTypeSelect(fuel)
                                        fuelMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }


            Button(
                onClick = onSearchClick,
                modifier = Modifier.fillMaxWidth(), // Botón de ancho completo
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.Search, contentDescription = "Buscar")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Buscar Coches")
            }
        }
    }
}


// --- DIÁLOGO DE FILTRO MARCA/MODELO ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandModelFilterDialog(
    brands: List<String>,
    models: List<String>,
    selectedBrand: String?,
    isLoadingBrands: Boolean,
    isLoadingModels: Boolean,
    brandLoadError: String?,
    modelLoadError: String?,
    onBrandSelected: (String) -> Unit,
    onModelSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var modelMenuExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Seleccionar Marca y Modelo") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Cerrar")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface) // Fondo del diálogo
            ) {
                // Sección de Marcas
                Text(
                    "Selecciona una Marca",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                if (isLoadingBrands) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                } else if (brandLoadError != null) {
                    Text(
                        "Error cargando marcas: $brandLoadError",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                } else if (brands.isEmpty()) {
                    Text("No hay marcas disponibles.", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f), // Ocupa espacio disponible para marcas
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(brands) { brand ->
                            ListItem(
                                headlineContent = { Text(brand) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onBrandSelected(brand) }
                                    .background(
                                        if (brand == selectedBrand) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Transparent
                                    )
                                    .padding(vertical = 8.dp) // Padding vertical para cada item
                            )
                            HorizontalDivider()
                        }
                    }
                }

                // Sección de Modelos (si se ha seleccionado una marca)
                if (selectedBrand != null) {
                    HorizontalDivider(
                        thickness = 4.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        "Selecciona un Modelo para $selectedBrand",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    if (isLoadingModels) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp)
                        )
                    } else if (modelLoadError != null) {
                        Text(
                            "Error cargando modelos: $modelLoadError",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else if (models.isEmpty() && !isLoadingModels) { // Asegurar que no está cargando
                        Text(
                            "No hay modelos disponibles para $selectedBrand.",
                            modifier = Modifier.padding(16.dp)
                        )
                    } else if (models.isNotEmpty()) {
                        // Usar un Dropdown aquí puede ser un poco extraño en un diálogo de pantalla completa
                        // Considera otra LazyColumn para modelos si la lista es larga.
                        // Por ahora, mantendremos el ExposedDropdownMenuBox como lo pediste.
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            ExposedDropdownMenuBox(
                                expanded = modelMenuExpanded,
                                onExpandedChange = { modelMenuExpanded = !modelMenuExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = "Seleccionar modelo...", // Placeholder
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Modelo") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = modelMenuExpanded
                                        )
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = modelMenuExpanded,
                                    onDismissRequest = { modelMenuExpanded = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    models.forEach { model ->
                                        DropdownMenuItem(
                                            text = { Text(model) },
                                            onClick = {
                                                onModelSelected(model)
                                                modelMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SECCIÓN DE ÚLTIMOS COCHES ---
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
    Column(modifier = Modifier.padding(vertical = 8.dp)) { // Reducido padding vertical
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (showSeeMoreButton && cars.isNotEmpty()) { // Mostrar "Ver más" solo si hay coches y es la sección de "últimos"
                TextButton(onClick = onSeeMoreClick) {
                    Text("Ver más")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) { // Altura ajustada
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Text(
                "Error: $error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else if (cars.isEmpty() && (title == "Últimos Coches Publicados" || title == "No se encontraron resultados")) {
            Text(
                if (title == "No se encontraron resultados") title else "No hay coches publicados recientemente.",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cars, key = { it.id }) { car ->
                    // Actualizar CarItemCard para pasar el estado de favorito
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


// --- TARJETA INDIVIDUAL DE COCHE ---
@Composable
fun CarItemCard(
    car: CarForSale,
    isUserAuthenticated: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current
    val carColorValue = remember(car.carColor) {
        CarColor.fromName(car.carColor)?.colorValue ?: Color.Transparent
    }

    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(180.dp) // Un poco más alto para la imagen
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Image(
                    painter = if (car.imageUrls.isNotEmpty()) {
                        rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(car.imageUrls.first())
                                .crossfade(true)
                                .error(R.drawable.no_photo) // Debes tener este drawable
                                .placeholder(R.drawable.no_photo) // Y este
                                .build()
                        )
                    } else {
                        // Placeholder si no hay imagen, usando un icono de coche
                        painterResource(id = R.drawable.no_photo)
                    },
                    contentDescription = "Imagen de ${car.brand} ${car.model}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Círculo de color del coche
                if (carColorValue != Color.Transparent) { // Solo mostrar si hay un color válido
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart) // Posición diferente
                            .padding(8.dp)
                            .size(28.dp) // Un poco más grande
                            .background(carColorValue, CircleShape)
                            .border(
                                1.5.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                CircleShape
                            ) // Borde más visible
                    )
                }

                // Botón de Favorito
                if (isUserAuthenticated) { // Solo mostrar si el usuario está autenticado
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), // Fondo semitransparente
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

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${car.brand} ${car.model}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = car.version,
                    style = MaterialTheme.typography.bodyMedium, // Un poco más grande
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${String.format("%,.0f", car.price).replace(",", ".")} €",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), // Un poco más grande
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = car.year, style = MaterialTheme.typography.bodySmall)
                    Text("•", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = car.fuelType,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
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
