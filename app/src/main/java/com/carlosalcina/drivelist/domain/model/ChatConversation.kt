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
    val buyerPhotoUrl: String? = null,

    val sellerId: String = "",
    val sellerName: String? = null,
    val sellerPhotoUrl: String? = null,

    val participantIds: List<String> = emptyList(),

    val lastMessageText: String? = null,
    @ServerTimestamp
    val lastMessageTimestamp: Timestamp? = null,
    val lastMessageSenderId: String? = null,

    val unreadCount: Map<String, Int> = emptyMap()
) {
    // Constructor sin argumentos necesario para la deserialización de Firestore
    constructor() : this(
        id = "",
        carId = null,
        carImageUrl = null,
        carName = null,
        buyerId = "",
        buyerName = null,
        buyerPhotoUrl = null,
        sellerId = "",
        sellerName = null,
        sellerPhotoUrl = null,
        participantIds = emptyList(),
        lastMessageText = null,
        lastMessageTimestamp = null,
        lastMessageSenderId = null,
        unreadCount = emptyMap()
    )

    fun getOtherParticipantId(currentUserId: String): String? {
        return participantIds.firstOrNull { it != currentUserId }
    }

    fun getParticipantDisplayName(participantId: String): String? {
        return when (participantId) {
            buyerId -> buyerName
            sellerId -> sellerName
            else -> null
        }
    }

    fun getParticipantPhotoUrl(participantId: String): String? {
        return when (participantId) {
            buyerId -> buyerPhotoUrl
            sellerId -> sellerPhotoUrl
            else -> null
        }
    }
}