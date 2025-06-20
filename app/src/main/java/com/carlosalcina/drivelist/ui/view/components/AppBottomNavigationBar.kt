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
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.carlosalcina.drivelist.ui.navigation.Screen

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

    // Función para comparar la ruta actual con una ruta base
    fun NavDestination.matchesRoute(baseRoute: String): Boolean {
        return hierarchy.any { it.route?.substringBefore('?') == baseRoute.substringBefore('?') }
    }

    val showBottomBar = bottomNavItems.any { screen ->
        currentDestination?.matchesRoute(screen.route) == true
    }

    if (showBottomBar) {
        NavigationBar(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            bottomNavItems.forEach { screen ->
                val isSelected = currentDestination?.matchesRoute(screen.route) == true
                NavigationBarItem(
                    icon = {
                        screen.icon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = stringResource(id = screen.resourceId)
                            )
                        }
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
                                restoreState = false
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.inverseSurface
                    )
                )
            }
        }
    }
}