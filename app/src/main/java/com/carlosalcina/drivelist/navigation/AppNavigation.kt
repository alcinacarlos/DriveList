package com.carlosalcina.drivelist.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.carlosalcina.drivelist.ui.view.screens.CarDetailScreen
import com.carlosalcina.drivelist.ui.view.screens.FavoritesScreen
import com.carlosalcina.drivelist.ui.view.screens.HomeScreen
import com.carlosalcina.drivelist.ui.view.screens.LoginScreen
import com.carlosalcina.drivelist.ui.view.screens.ProfileScreen
import com.carlosalcina.drivelist.ui.view.screens.RegisterScreen
import com.carlosalcina.drivelist.ui.view.screens.SearchVehicleScreen
import com.carlosalcina.drivelist.ui.view.screens.SettingsScreen
import com.carlosalcina.drivelist.ui.view.screens.UploadCarScreen
import com.carlosalcina.drivelist.utils.FirebaseUtils


@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLanguageChange: (String) -> Unit
) {
    val firebaseAuth = FirebaseUtils.getInstance()
    val currentUser = firebaseAuth.currentUser
    val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

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
                    navController.navigate(Screen.Home.route) {
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
        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument(NavigationArgs.PROFILE_USER_ID_ARG) { type = NavType.StringType })
        ) {
            ProfileScreen(
                onCarClicked = {
                    navController.navigate(Screen.CarDetail.createRoute(it))
                }
            )
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
            route = Screen.SearchVehicle.routeWithArgsTemplate(),
            arguments = listOf(
                navArgument(NavigationArgs.SEARCH_FILTERS_JSON_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(NavigationArgs.INITIAL_SEARCH_TERM_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            SearchVehicleScreen(navController= navController)
        }

        composable(
            route = Screen.CarDetail.route,
            arguments = listOf(navArgument(NavigationArgs.CAR_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            CarDetailScreen(
                onContactSeller = { sellerId, carId ->
                    navController.navigate(Screen.ChatDetail.createRoute(sellerId, carId))
                },
                onSeeProfile = { sellerId ->
                    navController.navigate(Screen.Profile.createRoute(sellerId))
                }
            )
        }

        composable(
            route = "chat_detail_screen/{sellerId}/{carId}",
            arguments = listOf(
                navArgument("sellerId") { type = NavType.StringType },
                navArgument("carId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sellerId = backStackEntry.arguments?.getString("sellerId")
            val carIdArg = backStackEntry.arguments?.getString("carId")
            // ChatDetailScreen(sellerId = sellerId, carId = carIdArg, onNavigateBack = { navController.popBackStack() })
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Pantalla de Chat (Pendiente)\nSeller: $sellerId\nCar: $carIdArg")
            }
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(navController)
        }
    }
}
