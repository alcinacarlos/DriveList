package com.carlosalcina.drivelist.ui.view.components

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.utils.Utils
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("DefaultLocale")
@Composable
fun CarItemCard(
    car: CarForSale,
    isUserAuthenticated: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val timeAgo = Utils.formatTimestamp(car.timestamp)
    val pagerState = rememberPagerState(pageCount = { car.imageUrls.size.coerceAtLeast(1) })
    val imageLoader = context.imageLoader

    // LÓGICA DE PRECARGA
    LaunchedEffect(pagerState.currentPage, car.imageUrls) {
        if (car.imageUrls.size > 1) {
            val currentPage = pagerState.currentPage
            val preloadCandidates = mutableListOf<String>()

            // Imagen siguiente
            val nextPageIndex = (currentPage + 1) % car.imageUrls.size
            if (nextPageIndex != currentPage) {
                preloadCandidates.add(car.imageUrls[nextPageIndex])
            }

            // Imagen anterior
            val prevPageIndex = (currentPage - 1 + car.imageUrls.size) % car.imageUrls.size
            if (prevPageIndex != currentPage) {
                preloadCandidates.add(car.imageUrls[prevPageIndex])
            }

            val twoAheadIndex = (currentPage + 2) % car.imageUrls.size
            if (twoAheadIndex != currentPage && twoAheadIndex != nextPageIndex) {
                preloadCandidates.add(car.imageUrls[twoAheadIndex])
            }


            preloadCandidates.distinct().forEach { imageUrlToPreload ->
                val request = ImageRequest.Builder(context)
                    .data(imageUrlToPreload)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()
                imageLoader.enqueue(request)
            }
        }
    }

    Card(
        modifier = modifier
            .width(240.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                if (car.imageUrls.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(car.imageUrls[page])
                                    .crossfade(true)
                                    .error(R.drawable.no_photo)
                                    .placeholder(R.drawable.no_photo)
                                    .build()
                            ),
                            contentDescription = "Imagen de ${car.brand} ${car.model}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.no_photo),
                        contentDescription = "No hay imagen",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }

                // Botones de navegación para el Pager
                if (car.imageUrls.size > 1) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val enabledBack = pagerState.currentPage > 0
                        IconButton(
                            enabled = enabledBack,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage((pagerState.currentPage - 1 + car.imageUrls.size) % car.imageUrls.size)
                                }
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                "Anterior",
                                tint = if (enabledBack) Color.White else Color.Transparent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage((pagerState.currentPage + 1) % car.imageUrls.size)
                                }
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                "Siguiente",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    // Indicador de página
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "${pagerState.currentPage + 1}/${car.imageUrls.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        )
                    }
                }


                // Botón de Favorito
                if (isUserAuthenticated) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    CircleShape
                                )
                        ) {

                            Icon(
                                imageVector = if (car.isFavoriteByCurrentUser) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (car.isFavoriteByCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }


            // DETALLES DEL COCHE
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${car.brand} ${car.model}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${String.format("%,.0f", car.price).replace(",", ".")} €",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )

                }

                if (car.version.isNotBlank()) {
                    Text(
                        text = car.version,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(2.dp))

                // Info chips: Año, KM, Combustible
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChipSmall(text = car.year)
                    InfoChipSmall(text = "${car.mileage} km")
                    InfoChipSmall(text = car.fuelType)
                }
                Spacer(Modifier.height(2.dp))

                // Ciudad y tiempo de publicación
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!car.ciudad.isNullOrBlank()) {
                        Text(
                            text = car.ciudad,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    Text(
                        text = timeAgo,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}