package com.carlosalcina.drivelist.domain.error

sealed class ChatError {
    data class NetworkError(val message: String = "Error de red. Por favor, comprueba tu conexión.") : ChatError()
    data class ConversationNotFound(val message: String = "Conversación no encontrada.") : ChatError()
    data class MessageSendError(val message: String = "Error al enviar el mensaje.") : ChatError()
    data class OperationFailed(val message: String = "La operación del chat ha fallado.") : ChatError()
    data class UserNotAuthenticated(val message: String = "Usuario no autenticado.") : ChatError()
    data class InsufficientPermissions(val message: String = "Permisos insuficientes.") : ChatError()
    data class UnknownError(val exception: Exception? = null, val message: String = "Ha ocurrido un error desconocido.") : ChatError()
}