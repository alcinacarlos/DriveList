package com.carlosalcina.drivelist.navigation

import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.domain.model.CarSearchFilters

sealed class Screen(
    val route: String, @StringRes val resourceId: Int, val icon: ImageVector? = null
) {
    object Welcome : Screen("welcome_screen", R.string.screen_title_welcome)
    object Login : Screen("login_screen", R.string.screen_title_login)
    object Register : Screen("register_screen", R.string.screen_title_register)
    object Profile : Screen(
        "profile_screen/{${NavigationArgs.PROFILE_USER_ID_ARG}}", R.string.screen_title_profile
    ) {
        fun createRoute(userId: String): String = "profile_screen/$userId"
    }

    object Settings : Screen("settings_screen", R.string.screen_title_settings)

    object Home : Screen("home_screen", R.string.screen_title_home, Icons.Filled.Home)
    object UploadCar :
        Screen("upload_car_screen", R.string.screen_title_sell, Icons.Filled.AddCircleOutline)

    object SearchVehicle :
        Screen("search_vehicle_screen", R.string.screen_title_search, Icons.Filled.Search) {
        fun routeWithArgsTemplate(): String {
            return "$route?${NavigationArgs.SEARCH_FILTERS_JSON_ARG}={${NavigationArgs.SEARCH_FILTERS_JSON_ARG}}&${NavigationArgs.INITIAL_SEARCH_TERM_ARG}={${NavigationArgs.INITIAL_SEARCH_TERM_ARG}}"
        }

        fun createRoute(filters: CarSearchFilters? = null, searchTerm: String? = null): String {
            val gson = com.google.gson.Gson()
            var path = route
            val queryParams = mutableListOf<String>()
            filters?.let {
                if (it != CarSearchFilters(searchTerm = null) && it != CarSearchFilters()) {
                    try {
                        val filtersJson = gson.toJson(it)
                        queryParams.add(
                            "${NavigationArgs.SEARCH_FILTERS_JSON_ARG}=${
                                Uri.encode(
                                    filtersJson
                                )
                            }"
                        )
                    } catch (e: Exception) {
                        Log.d("SearchVehicleScreen", "Error al convertir a JSON: ${e.message}")
                    }
                } else if (it.searchTerm != null && searchTerm == null) {
                    queryParams.add("${NavigationArgs.INITIAL_SEARCH_TERM_ARG}=${Uri.encode(it.searchTerm)}")
                }
            }
            if (searchTerm != null) {
                val existingSearchTermParamIndex =
                    queryParams.indexOfFirst { q -> q.startsWith(NavigationArgs.INITIAL_SEARCH_TERM_ARG) }
                if (existingSearchTermParamIndex != -1) queryParams.removeAt(
                    existingSearchTermParamIndex
                )
                queryParams.add("${NavigationArgs.INITIAL_SEARCH_TERM_ARG}=${Uri.encode(searchTerm)}")
            }
            if (queryParams.isNotEmpty()) path += "?" + queryParams.joinToString("&")
            return path
        }
    }

    object CarDetail : Screen(
        "car_detail_screen/{${NavigationArgs.CAR_ID_ARG}}", R.string.screen_title_car_detail
    ) {
        fun createRoute(carId: String): String = "car_detail_screen/$carId"
    }

    object Favorites :
        Screen("favorites_screen", R.string.screen_title_favorites, Icons.Filled.Favorite)

    object ChatDetail : Screen(
        "chat_detail_screen/{${NavigationArgs.CAR_ID_ARG}}/{${NavigationArgs.SELLER_ID_ARG}}/{${NavigationArgs.BUYER_ID_ARG}}",
        R.string.screen_title_chat_detail
    ) {
        fun createRoute(carId: String, sellerId: String, buyerId: String): String =
            "chat_detail_screen/$carId/$sellerId/$buyerId"
    }

    object EditVehicle : Screen("edit_vehicle_screen/{carId}", R.string.screen_title_edit) {
        fun createRoute(carId: String): String = "chat_detail_screen/$carId"
    }

    object ChatList :
        Screen("chat_list_screen", R.string.screen_title_chat, Icons.AutoMirrored.Filled.Chat)
}