package com.carlosalcina.drivelist.ui.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
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
        Screen.ChatList,
        Screen.CarDetail
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showTopBar = Screen.allScreens.any { screen ->
        screen.showTopBar == true
    }
    val currentScreen = bottomNavItems.find { screen ->
        Utils.matchDestination(currentRoute, screen.route)
    }

    if (showTopBar) {
        TopAppBar(
            title = {
                currentScreen?.let { Text(stringResource(id = it.resourceId)) }
                ?: Text(stringResource(id = R.string.app_name))
        },
            navigationIcon = {
                if (currentScreen?.showBackArrow == true){
                    IconButton(onClick = { navController.popBackStack() } ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }

            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }
}