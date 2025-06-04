package com.carlosalcina.drivelist.ui.states

import com.carlosalcina.drivelist.domain.model.ChatConversation

data class ChatListUiState(
    val isLoading: Boolean = false,
    val conversations: List<ChatConversation> = emptyList(),
    val error: String? = null,
    val currentUserId: String? = null
)