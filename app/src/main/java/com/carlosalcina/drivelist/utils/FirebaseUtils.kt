package com.carlosalcina.drivelist.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

object FirebaseUtils {
    private val storageInstance = FirebaseStorage.getInstance()
    private val firebaseInstance = FirebaseAuth.getInstance()

    fun getInstance() = firebaseInstance
    fun getStorage() = storageInstance
}