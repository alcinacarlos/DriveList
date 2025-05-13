package com.carlosalcina.drivelist.domain.repository

import android.content.Context
import com.carlosalcina.drivelist.domain.model.GoogleSignInError
import com.carlosalcina.drivelist.utils.Result

interface GoogleSignInHandler {
    suspend fun getGoogleIdToken(context: Context, serverClientId: String): Result<String, GoogleSignInError>
}