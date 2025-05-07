package com.carlosalcina.drivelist.ui.view.screens

//import coil.compose.rememberAsyncImagePainter // Removed Coil import
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(auth: FirebaseAuth, navController: NavController) {
    val currentUser = auth.currentUser
    // Si el usuario no está autenticado, mostrar mensaje o redirigir
    if (currentUser == null) {
        Text(text = "No user is signed in.")
        return
    }

    // Mostrar los detalles del usuario
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Foto de perfil
        currentUser.photoUrl?.let {
            Image(painter = rememberAsyncImagePainter(it), contentDescription = "Profile picture", modifier = Modifier.size(120.dp).clip(CircleShape))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre
        Text(text = "Name: ${currentUser.displayName ?: "No name"}", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(8.dp))

        // Email
        Text(text = "Email: ${currentUser.email ?: "No email"}", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(8.dp))

        // UID (ID único del usuario)
        Text(text = "UID: ${currentUser.uid}", style = MaterialTheme.typography.bodyMedium)
    }
    Button(onClick = {
        auth.signOut()
        navController.navigate("login") {
            popUpTo("home") { inclusive = true }
        }
    }) {
        Text("Cerrar sesión")
    }
}
