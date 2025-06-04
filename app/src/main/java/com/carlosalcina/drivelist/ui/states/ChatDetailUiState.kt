package com.carlosalcina.drivelist.ui.states

import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.ChatMessage
import com.carlosalcina.drivelist.domain.model.UserData

data class ChatDetailUiState(
    val isLoadingInitialData: Boolean = true, // Para la carga inicial de conversación, usuarios, coche
    val isLoadingMessages: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val error: String? = null,
    val currentMessageText: String = "",
    val conversationId: String? = null,
    val currentUserId: String? = null,
    val currentUserData: UserData? = null,
    val otherParticipant: UserData? = null, // Para nombre y foto en la AppBar
    val carDetails: CarForSale? = null, // Para mostrar info del coche
    val canSendMessage: Boolean = false // Se activa cuando conversationId y currentUserId están listos
)