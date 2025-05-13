package com.carlosalcina.drivelist.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object LanguageRepository {
    private lateinit var dataStore: DataStore<Preferences>
    private val LANGUAGE_KEY = stringPreferencesKey("language_code")
    const val DEFAULT_LANGUAGE = "es"

    fun initialize(context: Context) {
        if (!::dataStore.isInitialized) {
            dataStore = context.applicationContext.dataStore
        }
    }

    val language: Flow<String>
        get() {
            if (!::dataStore.isInitialized) {
                throw IllegalStateException("LanguageRepository must be initialized before accessing language flow.")
            }
            return dataStore.data.map { preferences ->
                preferences[LANGUAGE_KEY] ?: DEFAULT_LANGUAGE
            }
        }

    suspend fun saveLanguage(languageCode: String) {
        if (!::dataStore.isInitialized) {
            throw IllegalStateException("LanguageRepository must be initialized before saving language.")
        }
        dataStore.edit { settings ->
            settings[LANGUAGE_KEY] = languageCode
        }
    }
}