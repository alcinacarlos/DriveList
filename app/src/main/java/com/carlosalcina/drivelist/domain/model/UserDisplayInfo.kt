package com.carlosalcina.drivelist.domain.model

data class UserDisplayInfo(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val profilePictureUrl: String?
)