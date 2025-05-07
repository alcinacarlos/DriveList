package com.carlosalcina.drivelist.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.carlosalcina.drivelist.ui.view.screens.HomeScreen
import com.carlosalcina.drivelist.ui.view.screens.LoginScreen
import com.carlosalcina.drivelist.ui.view.screens.ProfileScreen
import com.carlosalcina.drivelist.ui.view.screens.RegisterScreen
import com.carlosalcina.drivelist.ui.viewmodel.LoginViewModel
import com.carlosalcina.drivelist.ui.viewmodel.RegisterViewModel
import com.carlosalcina.drivelist.utils.FirebaseUtils

@Composable
fun AppNavigation(navController: NavHostController) {
    val auth = FirebaseUtils.getInstance()
    val currentUser = auth.currentUser
    val startDestination = if (currentUser != null) "profile" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("welcome") {
            //WelcomeScreen(navController)
        }
        composable("login") {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val loginViewModel = LoginViewModel()

            LoginScreen(
                viewModel = loginViewModel,
                onLoginExitoso = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onIrARegistro = { navController.navigate("register") },
                onGoogleSignIn = {
                    loginViewModel.iniciarSesionConCredentialManager(
                        context,
                        scope,
                        onSuccess = {
                            navController.navigate("profile") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onError = { msg -> loginViewModel.estadoMensaje = msg }
                    )
                }
            )
        }
        composable("register") {
            val registerViewModel = RegisterViewModel()
            RegisterScreen(registerViewModel, onRegister = {
                navController.navigate("profile") {
                    popUpTo("register") { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeScreen(navController)
        }
        composable("profile") {
            ProfileScreen(auth, navController)
        }
    }
}