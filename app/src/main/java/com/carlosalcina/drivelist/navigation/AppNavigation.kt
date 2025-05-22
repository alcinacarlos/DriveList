package com.carlosalcina.drivelist.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.carlosalcina.drivelist.ui.view.screens.HomeScreen
import com.carlosalcina.drivelist.ui.view.screens.LoginScreen
import com.carlosalcina.drivelist.ui.view.screens.ProfileScreen
import com.carlosalcina.drivelist.ui.view.screens.RegisterScreen
import com.carlosalcina.drivelist.ui.view.screens.SearchVehicleScreen
import com.carlosalcina.drivelist.ui.view.screens.SettingsScreen
import com.carlosalcina.drivelist.ui.view.screens.UploadCarScreen
import com.carlosalcina.drivelist.utils.FirebaseUtils


@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier, onLanguageChange: (String) -> Unit) {
    val firebaseAuth = FirebaseUtils.getInstance()
    val currentUser = firebaseAuth.currentUser
    val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {

        composable(Screen.Welcome.route) {
            // WelcomeScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = hiltViewModel(),
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = hiltViewModel(),
                onNavigateOnSuccess = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onLanguageChange)
        }
        composable(route = Screen.Home.route) {
            HomeScreen(
                navController = navController
            )
        }

        composable(route = Screen.UploadCar.route) {
            UploadCarScreen(
                onBack = { navController.popBackStack() },
                onUploadSuccess = { navController.popBackStack() },
                onSettings = { navController.navigate("settings") }
            )
        }

        composable(
            route = Screen.SearchVehicle.routeWithArgsTemplate(), // Usar la plantilla con placeholders
            arguments = listOf(
                navArgument(NavigationArgs.SEARCH_FILTERS_JSON_ARG) {
                    type = NavType.StringType
                    nullable = true // Puede que no se pasen filtros
                    defaultValue = null
                },
                navArgument(NavigationArgs.INITIAL_SEARCH_TERM_ARG) {
                    type = NavType.StringType
                    nullable = true // Puede que no haya término de búsqueda inicial
                    defaultValue = null
                }
            )
        ) { /* backStackEntry ->
            // El SearchVehicleScreenViewModel ya obtiene los argumentos vía SavedStateHandle
            // No necesitas pasar backStackEntry.arguments aquí si usas SavedStateHandle en el ViewModel
            */
            SearchVehicleScreen(
                //navController = navController // Pasa si SearchVehicleScreen necesita navegar
            )
        }

        composable(
            route = Screen.CarDetail.route,
            arguments = listOf(navArgument(NavigationArgs.CAR_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString(NavigationArgs.CAR_ID_ARG)
            if (carId != null) {
                // CarDetailScreen(navController = navController, carId = carId)
                // Por ahora, un placeholder:
                // Text("Pantalla de Detalles del Coche para ID: $carId")
            } else {
                // Manejar el caso de carId nulo (no debería pasar si la ruta está bien definida)
                // Text("Error: ID de coche no encontrado")
            }
        }

        // Añade aquí tus otras pantallas (Login, Register, etc.)
    }
}