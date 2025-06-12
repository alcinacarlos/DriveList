package com.carlosalcina.drivelist

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.carlosalcina.drivelist.data.preferences.LanguageRepository
import com.carlosalcina.drivelist.data.preferences.ThemeRepository
import com.carlosalcina.drivelist.localization.LocaleHelper
import com.carlosalcina.drivelist.ui.navigation.AppNavigation
import com.carlosalcina.drivelist.ui.theme.DriveListTheme
import com.carlosalcina.drivelist.ui.theme.ThemeOption
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeRepository: ThemeRepository


    override fun attachBaseContext(newBase: Context) {
        val currentLanguage = runBlocking { LanguageRepository.language.first() }
        super.attachBaseContext(LocaleHelper.applyOverrideConfiguration(newBase, currentLanguage))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        setContent {
            val currentTheme by themeRepository.theme.collectAsState(initial = ThemeOption.SYSTEM_DEFAULT)
            val navController = rememberNavController()
            DriveListTheme(appTheme = currentTheme) {
                    AppNavigation(
                        navController,
                        themeRepository,
                        onLanguageChange = { newLang ->
                            lifecycleScope.launch {
                                LanguageRepository.saveLanguage(newLang)
                                recreate()
                            }
                        })
                }
            }
        }
    }
