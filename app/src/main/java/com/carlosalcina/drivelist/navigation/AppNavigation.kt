package com.carlosalcina.drivelist.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.carlosalcina.drivelist.ui.view.screens.CarDetailScreen
import com.carlosalcina.drivelist.ui.view.screens.ChatDetailScreen
import com.carlosalcina.drivelist.ui.view.screens.ChatListScreen
import com.carlosalcina.drivelist.ui.view.screens.EditCarScreen
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
                navController = navController,
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
                navController = navController,
                onUploadSuccess = { navController.popBackStack() },
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
                navController = navController,
                onContactSeller = { sellerId, carId, buyerId ->
                    navController.navigate(Screen.ChatDetail.createRoute(carId, sellerId, buyerId))
                },
                onSeeProfile = { sellerId ->
                    navController.navigate(Screen.Profile.createRoute(sellerId))
                },
                onEditCar = { carId ->
                    navController.navigate(Screen.EditVehicle.createRoute(carId))
                }
            )
        }

        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument(NavigationArgs.CAR_ID_ARG) { type = NavType.StringType },
                navArgument(NavigationArgs.SELLER_ID_ARG) { type = NavType.StringType },
                navArgument(NavigationArgs.BUYER_ID_ARG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            ChatDetailScreen(navController = navController)
        }

        composable(route = Screen.ChatList.route) {
            ChatListScreen(
                navController = navController
            )
        }
        composable(
            route = Screen.EditVehicle.route,
            arguments = listOf(navArgument(NavigationArgs.CAR_ID_ARG) { type = NavType.StringType })
        ) {
            EditCarScreen(
                navController = navController,
                onUpdateSuccess = {},
                carId = it.arguments?.getString(NavigationArgs.CAR_ID_ARG) ?: ""
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(navController)
        }
    }
}
