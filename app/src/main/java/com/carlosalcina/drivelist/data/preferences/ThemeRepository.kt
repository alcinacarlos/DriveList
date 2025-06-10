package com.carlosalcina.drivelist.data.preferences // O tu paquete de datos y preferencias

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.carlosalcina.drivelist.ui.theme.ThemeOption
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ThemeRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val APP_THEME = stringPreferencesKey("app_theme")
    }

    /**
     * Un Flow que emite la preferencia de tema actual cada vez que cambia.
     * Si no hay ninguna preferencia guardada, devuelve ThemeOption.SYSTEM_DEFAULT.
     */
    val theme: Flow<ThemeOption> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.APP_THEME] ?: ThemeOption.SYSTEM_DEFAULT.name
            try {
                ThemeOption.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                ThemeOption.SYSTEM_DEFAULT
            }
        }

    /**
     * Guarda la selección de tema del usuario de forma asíncrona.
     * @param themeOption El tema seleccionado por el usuario (LIGHT, DARK, o SYSTEM_DEFAULT).
     */
    suspend fun setTheme(themeOption: ThemeOption) {
        context.dataStore.edit { preferences ->
            // Guardar el nombre de la constante del enum como un String.
            preferences[PreferencesKeys.APP_THEME] = themeOption.name
        }
    }
}
