package com.carlosalcina.drivelist

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.carlosalcina.drivelist.data.preferences.LanguageRepository
import com.carlosalcina.drivelist.localization.LocaleHelper
import com.carlosalcina.drivelist.navigation.AppNavigation
import com.carlosalcina.drivelist.ui.theme.DriveListTheme
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val currentLanguage = runBlocking { LanguageRepository.language.first() }
        super.attachBaseContext(LocaleHelper.applyOverrideConfiguration(newBase, currentLanguage))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            DriveListTheme(dynamicColor = false) {
                AppNavigation(navController, onLanguageChange = { newLang ->
                    lifecycleScope.launch {
                        LanguageRepository.saveLanguage(newLang)
                        recreate()
                    }
                })
            }
        }
    }
}