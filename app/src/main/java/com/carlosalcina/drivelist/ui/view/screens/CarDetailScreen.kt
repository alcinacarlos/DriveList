package com.carlosalcina.drivelist.ui.view.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.domain.model.CarColor
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.ui.states.CarDataState
import com.carlosalcina.drivelist.ui.states.SellerUiState
import com.carlosalcina.drivelist.ui.view.components.TopBar
import com.carlosalcina.drivelist.ui.viewmodel.CarDetailViewModel
import com.carlosalcina.drivelist.utils.Utils.formatAdPublicationDate
import com.carlosalcina.drivelist.utils.Utils.formatPriceDetail
import com.carlosalcina.drivelist.utils.Utils.formatUserSinceDetail
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    viewModel: CarDetailViewModel = hiltViewModel(),
    onContactSeller: (carId: String, sellerId: String, buyerId: String) -> Unit,
    onSeeProfile: (sellerId: String) -> Unit,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val carState = uiState.carDataState
    val sellerState = uiState.sellerUiState

    Scaffold(
        topBar = {
            TopBar(navController,R.string.screen_title_car_detail , showBackArrow = true)
        },
        floatingActionButton = {

            if (carState is CarDataState.Success && sellerState is SellerUiState.Success) {
                if (uiState.isBuyer){
                    ExtendedFloatingActionButton(
                        onClick = { onContactSeller(carState.car.userId, carState.car.id, uiState.currentUserId) },
                        icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Contactar") },
                        text = { Text("Contactar al Vendedor") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }else{
                    ExtendedFloatingActionButton(
                        onClick = {  },
                        icon = { Icon(Icons.Filled.Edit, contentDescription = "Contactar") },
                        text = { Text("Editar") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }

            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->
        HandleCarDataState(
            modifier = Modifier.padding(innerPadding),
            carDataState = uiState.carDataState,
            sellerUiState = uiState.sellerUiState,
            imagePagerIndex = uiState.imagePagerIndex,
            onImagePageChanged = { index -> viewModel.onImagePageChanged(index) },
            onRetryLoadCar = { viewModel.retryLoadCarDetails() },
            onSeeProfile = {
                if (carState is CarDataState.Success){
                    onSeeProfile(carState.car.userId)
                }
            }
        )
    }
}

@Composable
private fun HandleCarDataState(
    modifier: Modifier = Modifier,
    carDataState: CarDataState,
    sellerUiState: SellerUiState,
    imagePagerIndex: Int,
    onImagePageChanged: (Int) -> Unit,
    onRetryLoadCar: () -> Unit,
    onSeeProfile:() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (carDataState) {
            is CarDataState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is CarDataState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(carDataState.message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetryLoadCar) {
                        Text("Reintentar")
                    }
                }
            }
            is CarDataState.Success -> {
                CarDetailContent(
                    car = carDataState.car,
                    sellerUiState = sellerUiState,
                    imagePagerIndex = imagePagerIndex,
                    onImagePageChanged = onImagePageChanged,
                    onSeeProfile = {
                        onSeeProfile()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CarDetailContent(
    car: CarForSale,
    sellerUiState: SellerUiState,
    imagePagerIndex: Int,
    onImagePageChanged: (Int) -> Unit,
    onSeeProfile: () -> Unit
) {
    val imagePagerState = rememberPagerState(
        initialPage = imagePagerIndex,
        pageCount = { car.imageUrls.size.coerceAtLeast(1) }
    )
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Notify ViewModel of page changes by user swipe
    LaunchedEffect(imagePagerState.currentPage) {
        if (imagePagerState.currentPage != imagePagerIndex) {
            onImagePageChanged(imagePagerState.currentPage)
        }
    }
    // Effect for preloading images
    LaunchedEffect(imagePagerState.currentPage, car.imageUrls) {
        if (car.imageUrls.size > 1) {
            val imageLoader = context.imageLoader
            val nextIndex = (imagePagerState.currentPage + 1) % car.imageUrls.size
            val prevIndex = (imagePagerState.currentPage - 1 + car.imageUrls.size) % car.imageUrls.size

            listOfNotNull(
                car.imageUrls.getOrNull(nextIndex)?.takeIf { nextIndex != imagePagerState.currentPage },
                car.imageUrls.getOrNull(prevIndex)?.takeIf { prevIndex != imagePagerState.currentPage }
            ).distinct().forEach { url ->
                val request = ImageRequest.Builder(context).data(url).diskCachePolicy(CachePolicy.ENABLED).build()
                imageLoader.enqueue(request)
            }
        }
    }


    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Image Carousel
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (car.imageUrls.isNotEmpty()) {
                    HorizontalPager(state = imagePagerState, modifier = Modifier.fillMaxSize()) { page ->
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(car.imageUrls[page]).crossfade(true)
                                .error(R.drawable.no_photo).placeholder(R.drawable.no_photo).build(),
                            contentDescription = "Imagen ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (car.imageUrls.size > 1) {
                        PagerControls(
                            currentPage = imagePagerState.currentPage,
                            pageCount = car.imageUrls.size,
                            onNext = { coroutineScope.launch { imagePagerState.animateScrollToPage(imagePagerState.currentPage + 1) } },
                            onPrevious = { coroutineScope.launch { imagePagerState.animateScrollToPage(imagePagerState.currentPage - 1) } }
                        )
                    }
                } else {
                    Image(painterResource(R.drawable.no_photo), "No hay imagen", Modifier
                        .fillMaxSize()
                        .padding(32.dp), contentScale = ContentScale.Fit)
                }
            }
        }

        // 2. Main Car Info
        item { MainCarInfoSectionDetail(car) }

        // 3. Car Attributes
        item { CarAttributesSectionDetail(car) }

        // 4. Description
        item { DescriptionSectionDetail(car.description) }

        // 5. Seller Info
        item { SellerInfoSectionDetail(sellerUiState, onClick = {
            onSeeProfile()
        }) }

        item { Spacer(Modifier.height(110.dp)) }
    }
}

@Composable
private fun PagerControls(currentPage: Int, pageCount: Int, onNext: () -> Unit, onPrevious: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (pageCount > 1) {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                (0 until pageCount).forEach { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (currentPage == index) MaterialTheme.colorScheme.primary else Color.LightGray)
                    )
                }
            }
            IconButton(
                onClick = onPrevious,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape),
                enabled = currentPage > 0
            ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Anterior", tint = Color.White) }

            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape),
                enabled = currentPage < pageCount - 1
            ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Siguiente", tint = Color.White) }
        }
    }
}


@Composable
private fun MainCarInfoSectionDetail(car: CarForSale) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("${car.brand} ${car.model}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (car.version.isNotBlank()) {
            Text(car.version, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        Text(formatPriceDetail(car.price), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(formatAdPublicationDate(car.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
    }
}

data class AttributeItemUIData(val icon: ImageVector, val label: String, val value: String?)

@Composable
private fun CarAttributesSectionDetail(car: CarForSale) {
    val attributes = listOfNotNull(
        AttributeItemUIData(Icons.Filled.Speed, "Kilometraje", "${car.mileage} km"),
        car.fuelType.takeIf { it.isNotBlank() }?.let { AttributeItemUIData(Icons.Filled.LocalGasStation, "Combustible", it) },
        car.year.takeIf { it.isNotBlank() }?.let { AttributeItemUIData(Icons.Filled.CalendarToday, "Año", it) },
        car.bodyType.takeIf { it.isNotBlank() }?.let { AttributeItemUIData(Icons.Filled.DirectionsCar, "Carrocería", it) },
        car.carColor.takeIf { it.isNotBlank() }?.let { AttributeItemUIData(Icons.Filled.Palette, "Color", stringResource(id = CarColor.fromName(it)!!.displayNameResId )) },
        car.ciudad.takeIf { it?.isNotBlank() == true }?.let { AttributeItemUIData(Icons.Filled.LocationCity, "Ubicación", it) }
    )

    if (attributes.isNotEmpty()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Características", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
            attributes.chunked(2).forEach { rowAttrs ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowAttrs.forEach { attr ->
                        AttributeCardDetail(attr.icon, attr.label, attr.value!!, Modifier.weight(1f))
                    }
                    if (rowAttrs.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun AttributeCardDetail(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier
            .padding(16.dp)
            .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DescriptionSectionDetail(description: String) {
    if (description.isNotBlank()) {
        Column(Modifier.padding(16.dp)) {
            Text("Descripción", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            Text(description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SellerInfoSectionDetail(sellerUiState: SellerUiState, onClick: () -> Unit) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, top=16.dp, bottom = 24.dp)) { // Adjusted bottom padding
        Text("Información del Vendedor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
        Card(
            modifier = Modifier.fillMaxWidth()
                .clickable{ onClick() },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
        ) {
            when (sellerUiState) {
                is SellerUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(32.dp))
                }
                is SellerUiState.Error -> {
                    Text(
                        sellerUiState.message,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                is SellerUiState.Success -> {
                    val seller = sellerUiState.userData
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(seller.photoURL)
                                .error(R.drawable.no_photo)
                                .placeholder(R.drawable.no_photo)
                                .transformations(CircleCropTransformation())
                                .build(),
                            contentDescription = "Avatar del vendedor",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(seller.displayName ?: "Vendedor Anónimo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(formatUserSinceDetail(seller.createdAt), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}