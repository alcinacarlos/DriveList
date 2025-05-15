package com.carlosalcina.drivelist.domain.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.carlosalcina.drivelist.R

enum class CarColor(
    @StringRes val displayNameResId: Int,
    val colorValue: Color
) {
    FIRE_RED(R.string.car_color_fire_red, Color(0xFFD32F2F)),
    OCEAN_BLUE(R.string.car_color_ocean_blue, Color(0xFF1976D2)),
    EMERALD_GREEN(R.string.car_color_emerald_green, Color(0xFF388E3C)),
    MIDNIGHT_BLACK(R.string.car_color_midnight_black, Color.Black),
    SNOW_WHITE(R.string.car_color_snow_white, Color.White),
    SILVER_STONE(R.string.car_color_silver_stone, Color(0xFFBDBDBD)),
    SUNGLOW_YELLOW(R.string.car_color_sunglow_yellow, Color(0xFFFBC02D)),
    VIBRANT_ORANGE(R.string.car_color_vibrant_orange, Color(0xFFF57C00)),
    ROYAL_PURPLE(R.string.car_color_royal_purple, Color(0xFF7B1FA2)),
    GRAPHITE_GRAY(R.string.car_color_graphite_gray, Color(0xFF616161));

    companion object {
        fun GAMA_COMPLETA(): List<CarColor> = CarColor.entries
    }
}