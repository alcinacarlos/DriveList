package com.carlosalcina.drivelist.ui.theme

import androidx.annotation.StringRes
import com.carlosalcina.drivelist.R

enum class ThemeOption(@StringRes val displayNameResId: Int) {
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark),
    SYSTEM_DEFAULT(R.string.theme_system)
}