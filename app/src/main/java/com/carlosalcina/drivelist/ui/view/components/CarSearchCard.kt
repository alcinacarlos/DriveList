package com.carlosalcina.drivelist.ui.view.components

// import com.carlosalcina.drivelist.domain.model.CarColor // Your CarColor definition
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
import androidx.compose.material3.CircularProgressIndicator
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
fun CarSearchCard(
    car: CarForSale,
    isUserAuthenticated: Boolean,
    isTogglingFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column {
            //IMAGE SLIDER
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
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
                            contentDescription = "Imagen de ${car.brand} ${car.model} (${page + 1} de ${car.imageUrls.size})",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // Placeholder if no images
                    Image(
                        painter = painterResource(id = R.drawable.no_photo),
                        contentDescription = "No hay imagen disponible",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                    )
                }

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
                                    val prevPage = (pagerState.currentPage - 1 + car.imageUrls.size) % car.imageUrls.size
                                    pagerState.animateScrollToPage(prevPage)
                                }
                            },
                            modifier = Modifier
                                .background(if (enabledBack) Color.Black.copy(alpha = 0.3f) else Color.Transparent, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Imagen anterior",
                                tint = if (enabledBack) Color.White else Color.Transparent
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val nextPage = (pagerState.currentPage + 1) % car.imageUrls.size
                                    pagerState.animateScrollToPage(nextPage)
                                }
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Siguiente imagen",
                                tint = Color.White
                            )
                        }
                    }

                    // Image count indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${car.imageUrls.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }


                // Favorite Button
                if (isUserAuthenticated) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    CircleShape
                                )
                                .size(40.dp)
                        ) {
                            if (isTogglingFavorite) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
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

            //CAR DETAILS
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${car.brand} ${car.model}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${String.format("%,.0f", car.price).replace(",", ".")} €",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }

                if (car.version.isNotBlank()) {
                    Text(
                        text = car.version,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start), // Align chips to the start
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip(text = car.year)
                    InfoChip(text = "${car.mileage} km")
                    InfoChip(text = car.fuelType, maxLines = 1)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!car.ciudad.isNullOrBlank()) {
                        Text(
                            text = car.ciudad,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    Text(
                        text = timeAgo, // Display time ago
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}