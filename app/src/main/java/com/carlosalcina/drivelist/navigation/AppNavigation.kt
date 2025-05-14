package com.carlosalcina.drivelist.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.carlosalcina.drivelist.ui.view.screens.HomeScreen
import com.carlosalcina.drivelist.ui.view.screens.LoginScreen
import com.carlosalcina.drivelist.ui.view.screens.ProfileScreen
import com.carlosalcina.drivelist.ui.view.screens.RegisterScreen
import com.carlosalcina.drivelist.ui.view.screens.SettingsScreen
import com.carlosalcina.drivelist.ui.view.screens.UploadCarScreen
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AppNavigation(navController: NavHostController, onLanguageChange: (String) -> Unit) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser
    val startDestination = if (currentUser != null) "upload_car" else "register"

    NavHost(navController = navController, startDestination = startDestination) {

        composable("welcome") {
            // WelcomeScreen(navController)
        }
        composable("login") {
            LoginScreen(
                viewModel = hiltViewModel(),
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            RegisterScreen(
                viewModel = hiltViewModel(),
                onNavigateOnSuccess = {
                    navController.navigate("profile") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(navController)
        }
        composable("profile") {
            ProfileScreen(navController)
        }
        composable("settings") {
            SettingsScreen(onLanguageChange)
        }
        composable("upload_car") {
            UploadCarScreen(
                viewModel = hiltViewModel()
            )
        }
    }
}