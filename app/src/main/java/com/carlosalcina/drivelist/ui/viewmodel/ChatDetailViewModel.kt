package com.carlosalcina.drivelist.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.domain.error.ChatError
import com.carlosalcina.drivelist.domain.repository.AuthRepository
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.domain.repository.ChatRepository
import com.carlosalcina.drivelist.ui.states.ChatDetailUiState
import com.carlosalcina.drivelist.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val carListRepository: CarListRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    // Argumentos de navegación
    private val carIdArg: String? = savedStateHandle["carId"]
    private val sellerIdArg: String? = savedStateHandle["sellerId"]
    private val buyerIdArg: String? = savedStateHandle["buyerId"]

    init {
        initializeChat()
    }

    private fun initializeChat() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingInitialData = true, error = null)

            if (sellerIdArg == null || carIdArg == null || buyerIdArg == null) {
                _uiState.value = _uiState.value.copy(
                    isLoadingInitialData = false,
                    error = "Faltan datos para iniciar el chat."
                )
                return@launch
            }

            // 1. Obtener usuario actual
            val currentUserResult = authRepository.getCurrentUserData()
            val currentUserId = authRepository.getCurrentFirebaseUser()?.uid
            if (currentUserResult !is Result.Success || currentUserId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoadingInitialData = false,
                    error = "No se pudo obtener el usuario actual."
                )
                return@launch
            }
            val currentUserData = currentUserResult.data
            _uiState.value = _uiState.value.copy(
                currentUserId = currentUserId,
                currentUserData = currentUserData
            )


            // 2. Determinar quién es el otro participante y obtener sus datos
            val otherParticipantId = if (currentUserId == sellerIdArg) buyerIdArg else sellerIdArg

            val otherParticipantResult = authRepository.getUserData(otherParticipantId)
            when (otherParticipantResult) {
                is Result.Success -> {
                    _uiState.value =
                        _uiState.value.copy(otherParticipant = otherParticipantResult.data)
                }

                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingInitialData = false,
                        error = "No se pudo obtener la información del otro participante."
                    )
                    return@launch
                }

            }

            // 3. Obtener datos del coche
            val carResult = carListRepository.getCarById(carIdArg, currentUserId)
            if (carResult !is Result.Success) {
                _uiState.value = _uiState.value.copy(
                    isLoadingInitialData = false,
                    error = "No se pudo obtener la información del coche."
                )
                return@launch
            }
            val carData = carResult.data
            _uiState.value = _uiState.value.copy(carDetails = carData)

            val conversationResult = chatRepository.createOrGetConversation(
                currentUser = currentUserData,
                otherUser = _uiState.value.otherParticipant!!,
                car = carData
            )

            when (conversationResult) {
                is Result.Success -> {
                    val convId = conversationResult.data
                    _uiState.value = _uiState.value.copy(
                        conversationId = convId,
                        isLoadingInitialData = false,
                        canSendMessage = true
                    )
                    loadMessages(convId)
                    //markConversationAsRead(convId, currentUserId)
                }

                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingInitialData = false,
                        error = mapChatErrorToString(conversationResult.error)
                    )
                }
            }
        }
    }

    fun onScreenResumed() {
        val state = _uiState.value
        val convId = state.conversationId
        val userId = state.currentUserId

        if (convId != null && userId != null) {
            viewModelScope.launch {
                // 1. Marcar la conversación como leída
                chatRepository.markConversationAsRead(convId, userId)
                // 2. Marcar todos los mensajes como leídos
                chatRepository.markAllMessagesAsReadInConversation(convId, userId)

            }
        }
    }

    private fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMessages = true)
            chatRepository.getMessages(conversationId).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            messages = result.data, isLoadingMessages = false, error = null
                        )
                    }

                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingMessages = false, error = mapChatErrorToString(result.error)
                        )
                    }
                }
            }
        }
    }

    fun onMessageTextChanged(newText: String) {
        _uiState.value = _uiState.value.copy(currentMessageText = newText)
    }

    fun sendMessage() {
        val state = _uiState.value
        if (state.conversationId == null || state.currentUserId == null || state.currentUserData == null || state.otherParticipant == null) {
            _uiState.value = state.copy(error = "No se puede enviar el mensaje. Datos incompletos.")
            return
        }
        if (state.currentMessageText.isBlank()) {
            _uiState.value = state.copy(error = "El mensaje no puede estar vacío.")
            return
        }

        viewModelScope.launch {
            val result = chatRepository.sendMessage(
                conversationId = state.conversationId,
                senderId = state.currentUserId,
                senderName = state.currentUserData.displayName,
                senderPhotoUrl = state.currentUserData.photoURL,
                receiverId = state.otherParticipant.uid,
                text = state.currentMessageText,
                imageUrl = null // TODO: Implementar envío de imágenes si es necesario
            )

            when (result) {
                is Result.Success -> {
                    _uiState.value = state.copy(currentMessageText = "")
                }

                is Result.Error -> {
                    _uiState.value = state.copy(error = mapChatErrorToString(result.error))
                }
            }
        }
    }

    private fun markConversationAsRead(conversationId: String, userId: String) {
        viewModelScope.launch {
            chatRepository.markConversationAsRead(conversationId, userId)
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
}