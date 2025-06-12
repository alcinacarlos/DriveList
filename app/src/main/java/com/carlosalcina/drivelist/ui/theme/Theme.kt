package com.carlosalcina.drivelist.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Color(0xFF0f172a),
    surface = Color(0xFF0f172a),
    onPrimary = Color.White,
    onSecondary = OnSecondary,
    onBackground = Color.White,
    onSurface = OnSurface,
    error = Error,
    primaryContainer = Color(0xFF1B2537),
    inverseSurface = Color(0xFF112C55),
    inverseOnSurface = Color.White,
    inversePrimary = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    background = Background,
    surface = Surface,
    onPrimary = Color.White,
    onSecondary = OnSecondary,
    onBackground = OnBackground,
    onSurface = Color(0xFF98CBD9),
    error = Error,
    primaryContainer = Color.White,
    inverseSurface = Primary,
    inverseOnSurface = Color.Black,
    inversePrimary = Color.Black
)


@Composable
fun DriveListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: ThemeOption,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val scheme = when (appTheme) {
        ThemeOption.LIGHT -> LightColorScheme
        ThemeOption.DARK -> DarkColorScheme
        ThemeOption.SYSTEM_DEFAULT -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = typographyCustom(appTheme),
        content = content
    )
}