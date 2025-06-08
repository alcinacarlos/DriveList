package com.carlosalcina.drivelist.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class ChatConversation(
    val id: String = "",
    val carId: String? = null,
    val carImageUrl: String? = null,
    val carName: String? = null,

    val buyerId: String = "",
    val buyerName: String? = null,

    val sellerId: String = "",
    val sellerName: String? = null,

    val participantIds: List<String> = emptyList(),

    val lastMessageText: String? = null,
    @ServerTimestamp
    val lastMessageTimestamp: Timestamp? = null,
    val lastMessageSenderId: String? = null,

    val unreadCount: Map<String, Int> = emptyMap()
)