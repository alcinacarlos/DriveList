package com.carlosalcina.drivelist.ui.view.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.navigation.Screen
import com.carlosalcina.drivelist.utils.Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavController
) {
    val bottomNavItems = listOf(
        Screen.Home,
        Screen.SearchVehicle,
        Screen.UploadCar,
        Screen.Favorites,
        Screen.ChatList
    )
    // Solo mostrar la barra si estamos en una de las pantallas principales de la bottom nav
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBottomBar = bottomNavItems.any { screen ->
        Utils.matchDestination(currentRoute, screen.route)
                || navBackStackEntry?.destination?.hierarchy?.any { Utils.matchDestination(it.route, screen.route) } == true
    }
    val currentScreen = bottomNavItems.find { screen ->
        Utils.matchDestination(currentRoute, screen.route)
    }

    if (showBottomBar) {
        TopAppBar(
            title = {
                currentScreen?.let { Text(stringResource(id = it.resourceId)) }
                ?: Text(stringResource(id = R.string.app_name))
        },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }
}