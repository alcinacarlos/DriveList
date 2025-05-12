package com.carlosalcina.drivelist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.carlosalcina.drivelist.navigation.AppNavigation
import com.carlosalcina.drivelist.ui.theme.DriveListTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.runBlocking
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import com.carlosalcina.drivelist.data.preferences.LanguageDataStore
import com.carlosalcina.drivelist.localization.LocaleHelper
import com.carlosalcina.drivelist.localization.LocalizedContext
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        val initialLanguage = runBlocking {
            LanguageDataStore.getLanguage(this@MainActivity).first()
        }
        setContent {
            var language by remember { mutableStateOf(initialLanguage) }

            val localizedContext = LocaleHelper.setLocale(this, language)

            val navController = rememberNavController()
            CompositionLocalProvider(LocalizedContext provides localizedContext) {
                DriveListTheme(dynamicColor = false) {
                    AppNavigation(navController, onLanguageChange = { newLang ->
                        language = newLang
                        lifecycleScope.launch {
                            LanguageDataStore.saveLanguage(this@MainActivity, newLang)
                        }
                    })
                }
            }
        }
    }
}