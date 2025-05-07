package com.carlosalcina.drivelist.utils

import com.google.firebase.auth.FirebaseAuth

object FirebaseUtils {
    private val firebaseinstance = FirebaseAuth.getInstance()
    fun getInstance() = firebaseinstance
}