package com.carlosalcina.drivelist // O tu paquete raíz

import android.app.Application
import com.carlosalcina.drivelist.data.preferences.LanguageRepository
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DriveListApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LanguageRepository.initialize(this)
        FirebaseApp.initializeApp(this)
    }
}