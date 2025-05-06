package com.carlosalcina.drivelist.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.carlosalcina.drivelist.ui.view.screens.HomeScreen
import com.carlosalcina.drivelist.ui.view.screens.LoginScreen
import com.carlosalcina.drivelist.ui.view.screens.RegisterScreen
import com.carlosalcina.drivelist.ui.viewmodel.LoginViewModel
import com.carlosalcina.drivelist.ui.viewmodel.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(navController: NavHostController) {

    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("welcome") {
            //WelcomeScreen(navController)
        }
        composable("login") {
            val viewModel = LoginViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginExitoso = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onIrARegistro = {
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            val registerViewModel = RegisterViewModel()
            RegisterScreen(registerViewModel, onRegister = {
                navController.navigate("home") {
                    popUpTo("register") { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeScreen()
        }
    }
}