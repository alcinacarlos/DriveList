package com.carlosalcina.drivelist.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class UserData(
    @get:PropertyName("uid") @set:PropertyName("uid") var uid: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String? = null,
    @get:PropertyName("displayName") @set:PropertyName("displayName") var displayName: String? = null,
    @get:PropertyName("photoURL") @set:PropertyName("photoURL") var photoURL: String? = null,
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Timestamp? = null
)