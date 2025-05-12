package com.carlosalcina.drivelist.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

object LanguageDataStore {
    private val LANGUAGE_KEY = stringPreferencesKey("language")

    fun getLanguage(context: Context): Flow<String> {
        return context.dataStore.data.map { it[LANGUAGE_KEY] ?: "es" }
    }

    suspend fun saveLanguage(context: Context, language: String) {
        context.dataStore.edit { it[LANGUAGE_KEY] = language }
    }
}
