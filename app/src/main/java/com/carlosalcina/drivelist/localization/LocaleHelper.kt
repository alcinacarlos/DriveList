package com.carlosalcina.drivelist.localization

import android.content.Context
import android.content.res.Configuration
import com.carlosalcina.drivelist.data.preferences.LanguageRepository
import java.util.Locale

object LocaleHelper {
    fun applyOverrideConfiguration(context: Context, languageCode: String?): Context {
        val lang = languageCode ?: LanguageRepository.DEFAULT_LANGUAGE
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val newConfig = Configuration(context.resources.configuration)
        newConfig.setLocale(locale)

        return context.createConfigurationContext(newConfig)
    }
}