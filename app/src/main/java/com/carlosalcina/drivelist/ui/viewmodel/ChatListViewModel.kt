package com.carlosalcina.drivelist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.domain.error.ChatError
import com.carlosalcina.drivelist.domain.repository.AuthRepository
import com.carlosalcina.drivelist.domain.repository.ChatRepository
import com.carlosalcina.drivelist.ui.states.ChatListUiState
import com.carlosalcina.drivelist.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    fun loadConversations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val currentUser = authRepository.getCurrentFirebaseUser()
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Usuario no autenticado. Por favor, inicia sesión."
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(currentUserId = currentUser.uid)

            chatRepository.getChatConversations(currentUser.uid).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            conversations = result.data,
                            error = null
                        )
                    }
                    is Result.Error -> {
                        val errorMessage = mapChatErrorToString(result.error)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                }
            }
        }
    }

    private fun mapChatErrorToString(error: ChatError): String {
        return when (error) {
            is ChatError.NetworkError -> error.message
            is ChatError.ConversationNotFound -> error.message
            is ChatError.MessageSendError -> error.message
            is ChatError.OperationFailed -> error.message
            is ChatError.UserNotAuthenticated -> error.message
            is ChatError.InsufficientPermissions -> error.message
            is ChatError.UnknownError -> error.message
        }
    }

    // Opcional: Si quieres permitir refrescar la lista manualmente
    fun refreshConversations() {
        loadConversations()
    }
}