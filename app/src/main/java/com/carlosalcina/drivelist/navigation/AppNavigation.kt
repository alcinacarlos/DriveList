package com.carlosalcina.drivelist.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.carlosalcina.drivelist.data.repository.CredentialManagerGoogleSignInHandler
import com.carlosalcina.drivelist.data.repository.FirebaseAuthRepository
import com.carlosalcina.drivelist.domain.repository.GoogleSignInHandler
import com.carlosalcina.drivelist.ui.view.screens.HomeScreen
import com.carlosalcina.drivelist.ui.view.screens.LoginScreen
import com.carlosalcina.drivelist.ui.view.screens.ProfileScreen
import com.carlosalcina.drivelist.ui.view.screens.RegisterScreen
import com.carlosalcina.drivelist.ui.view.screens.SettingsScreen
import com.carlosalcina.drivelist.ui.viewmodel.LoginViewModel
import com.carlosalcina.drivelist.ui.viewmodel.RegisterViewModel
import com.carlosalcina.drivelist.utils.FirebaseUtils

@Composable
fun AppNavigation(navController: NavHostController, onLanguageChange: (String) -> Unit) {
    val auth = FirebaseUtils.getInstance()
    val authRepository = FirebaseAuthRepository(auth)
    val googleSignInHandler = CredentialManagerGoogleSignInHandler()

    val currentUser = auth.currentUser
    val startDestination = if (currentUser != null) "profile" else "register"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("welcome") {
            //WelcomeScreen(navController)
        }
        composable("login") {
            val loginViewModel = LoginViewModel(authRepository, googleSignInHandler)

            LoginScreen(
                viewModel = loginViewModel,
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
            val registerViewModel = RegisterViewModel(authRepository, googleSignInHandler)

            RegisterScreen(
                registerViewModel, onNavigateOnSuccess = {
                    navController.navigate("profile") {
                        popUpTo("register") { inclusive = true }
                    }
                }, onNavigateToLogin = {
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
            ProfileScreen(auth, navController)
        }
        composable("settings") {
            SettingsScreen(onLanguageChange)
        }
    }
}