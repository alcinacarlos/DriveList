package com.carlosalcina.drivelist.localization

import android.content.Context
import androidx.compose.runtime.compositionLocalOf

val LocalizedContext = compositionLocalOf<Context> {
    error("LocalizedContext not provided")
}
