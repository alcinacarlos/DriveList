package com.carlosalcina.drivelist.utils

import com.google.firebase.auth.FirebaseAuth

object FirebaseUtils {
    private val firebaseAuth= FirebaseAuth.getInstance()
    fun getInstance() = firebaseAuth
}