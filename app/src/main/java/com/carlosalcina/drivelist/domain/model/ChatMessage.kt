package com.carlosalcina.drivelist.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String? = null,
    val senderPhotoUrl: String? = null,
    val receiverId: String = "",
    val text: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val isRead: Boolean = false,
    val imageUrl: String? = null
) {
    // Constructor sin argumentos necesario para la deserialización de Firestore
    constructor() : this("", "", "", null, null, "", "", null, false, null)
}