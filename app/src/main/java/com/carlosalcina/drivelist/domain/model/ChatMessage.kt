package com.carlosalcina.drivelist.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val messageReaded: Boolean = false,
    val imageUrl: String? = null
)