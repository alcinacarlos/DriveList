package com.carlosalcina.drivelist.localization

import androidx.annotation.StringRes
import com.carlosalcina.drivelist.R

data class LanguageItem(
    val code: String,
    @StringRes val displayNameResId: Int
)

val availableLanguages = listOf(
    LanguageItem(code = "en", displayNameResId = R.string.language_display_en),
    LanguageItem(code = "es", displayNameResId = R.string.language_display_es)
)