package com.carlosalcina.drivelist.ui.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carlosalcina.drivelist.BuildConfig
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.ui.view.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopBar(navController,R.string.screen_about , showBackArrow = true)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sección del Logo y Título
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "Logo de la Aplicación",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tu mercado de confianza para coches de segunda mano",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                AboutSectionCard(title = "¿Qué es DriveList?") {
                    Text(
                        text = "DriveList es la plataforma definitiva para comprar y vender coches de segunda mano de forma fácil, rápida y segura. Nuestra misión es conectar a compradores y vendedores apasionados por el motor, ofreciendo una experiencia transparente y completa, desde la búsqueda detallada hasta el contacto directo.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                AboutSectionCard(title = "Funcionalidades Clave") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FeatureItem(
                            icon = Icons.Filled.FileUpload,
                            title = "Publicación Fácil y Detallada",
                            description = "Sube tu coche en minutos con nuestro formulario guiado. Añade múltiples imágenes, selecciona marca, modelo, color y todos los detalles técnicos para atraer a los compradores adecuados."
                        )
                        FeatureItem(
                            icon = Icons.Filled.Search,
                            title = "Búsqueda Avanzada y Filtros Rápidos",
                            description = "Encuentra el coche de tus sueños con una barra de búsqueda inteligente y filtros avanzados por marca, modelo, precio, año, ubicación y más."
                        )
                        FeatureItem(
                            icon = Icons.Filled.Favorite,
                            title = "Guarda tus Favoritos",
                            description = "No pierdas de vista los coches que te interesan. Márcalos como favoritos para acceder a ellos fácilmente y seguir su estado."
                        )
                        FeatureItem(
                            icon = Icons.Filled.Edit,
                            title = "Gestión de Anuncios",
                            description = "Si eres el propietario de un anuncio, puedes editar todos sus detalles, incluyendo imágenes y precio, en cualquier momento."
                        )
                        FeatureItem(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            title = "Chat Integrado",
                            description = "Comunícate directamente con los vendedores de forma segura a través de nuestro chat integrado para resolver dudas o concertar una visita."
                        )
                        FeatureItem(
                            icon = Icons.Filled.VerifiedUser,
                            title = "Autenticación Segura",
                            description = "La plataforma utiliza la autenticación de Firebase para garantizar que las interacciones se realicen entre usuarios verificados, aportando una capa extra de confianza."
                        )
                    }
                }
            }

            // Sección de Información Adicional
            item {
                AboutSectionCard(title = "Información Adicional") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Versión de la aplicación: ${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Desarrollado con ❤️ por Carlos Alcina", // ¡Puedes poner tu nombre!
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Contacto: carlos@alcina.es",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp).padding(top = 4.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(5.dp)
            )
        }
    }
}
