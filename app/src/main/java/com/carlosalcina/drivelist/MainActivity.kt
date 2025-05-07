package com.carlosalcina.drivelist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.carlosalcina.drivelist.navigation.AppNavigation
import com.carlosalcina.drivelist.ui.theme.DriveListTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            val navController = rememberNavController()
            DriveListTheme(dynamicColor = false) {
                AppNavigation(navController)
            }
        }
    }
}