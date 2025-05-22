package com.carlosalcina.drivelist.ui.view.components
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.carlosalcina.drivelist.navigation.Screen
import com.carlosalcina.drivelist.utils.Utils

@Composable
fun AppBottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val bottomNavItems = listOf(
        Screen.Home,
        Screen.SearchVehicle,
        Screen.UploadCar,
        Screen.Favorites,
        Screen.ChatList
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBottomBar = bottomNavItems.any { screen ->
        Utils.matchDestination(currentRoute, screen.route)
                || navBackStackEntry?.destination?.hierarchy?.any { Utils.matchDestination(it.route, screen.route) } == true
    }

    if (showBottomBar) {
        NavigationBar(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            bottomNavItems.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = screen.icon!!,
                            contentDescription = stringResource(id = screen.resourceId)
                        )
                    },
                    label = { Text(stringResource(id = screen.resourceId)) },
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}