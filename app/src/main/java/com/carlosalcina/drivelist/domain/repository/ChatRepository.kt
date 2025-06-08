package com.carlosalcina.drivelist.domain.repository

import com.carlosalcina.drivelist.domain.error.ChatError
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.ChatConversation
import com.carlosalcina.drivelist.domain.model.ChatMessage
import com.carlosalcina.drivelist.domain.model.UserData
import com.carlosalcina.drivelist.utils.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    /**
     * Obtiene o crea una conversación entre dos usuarios sobre un coche específico.
     * Si la conversación ya existe, la devuelve. Si no, la crea con los datos proporcionados.
     * El ID de la conversación se genera de forma predecible (ej: ordenando UIDs + carId).
     *
     * @param currentUser La información del usuario actual (comprador o vendedor).
     * @param otherUser La información del otro participante.
     * @param car El coche sobre el que trata la conversación.
     * @return Un Result que contiene el ID de la conversación (String) o un ChatError.
     */
    suspend fun createOrGetConversation(
        currentUser: UserData, // Quien inicia o participa en el chat
        otherUser: UserData,   // El otro participante
        car: CarForSale        // El coche en cuestión
    ): Result<String, ChatError> // Devuelve el ID de la conversación

    /**
     * Obtiene un Flow con la lista de todas las conversaciones de chat para un usuario específico.
     * La lista se actualiza en tiempo real.
     *
     * @param userId El ID del usuario para el cual obtener las conversaciones.
     * @return Un Flow que emite Result<List<ChatConversation>, ChatError>.
     */
    fun getChatConversations(userId: String): Flow<Result<List<ChatConversation>, ChatError>>

    /**
     * Marca todos los mensajes NO LEÍDOS de una conversación como LEÍDOS para un usuario específico.
     * Esta función es para implementar los "doble ticks".
     *
     * @param conversationId El ID de la conversación.
     * @param readerId El ID del usuario que está leyendo los mensajes.
     * @return Un Result que indica éxito (Unit) o un ChatError.
     */
    suspend fun markAllMessagesAsReadInConversation(
        conversationId: String,
        readerId: String
    ): Result<Unit, ChatError>

    /**
     * Obtiene un Flow con la lista de mensajes para una conversación específica.
     * La lista se actualiza en tiempo real y los mensajes se ordenan por timestamp.
     *
     * @param conversationId El ID de la conversación.
     * @return Un Flow que emite Result<List<ChatMessage>, ChatError>.
     */
    fun getMessages(conversationId: String): Flow<Result<List<ChatMessage>, ChatError>>

    /**
     * Envía un mensaje en una conversación.
     * Este método también debería actualizar la información del 'último mensaje' y
     * los contadores de no leídos en el documento de ChatConversation correspondiente.
     *
     * @param conversationId El ID de la conversación.
     * @param senderId El ID del remitente del mensaje.
     * @param receiverId El ID del destinatario del mensaje.
     * @param text El contenido del mensaje de texto.
     * @param imageUrl URL de la imagen (opcional).
     * @return Un Result que indica éxito (Unit) o un ChatError.
     */
    suspend fun sendMessage(
        conversationId: String,
        senderId: String, // UID del que envía
        senderName: String?, // Nombre para ChatMessage (puede venir de UserData)
        senderPhotoUrl: String?, // Foto para ChatMessage
        receiverId: String, // UID del que recibe
        text: String,
        imageUrl: String? = null
    ): Result<Unit, ChatError>

    /**
     * Marca todos los mensajes de una conversación como leídos para un usuario específico.
     * Esto implicaría actualizar el contador `unreadCount` en el documento `ChatConversation`.
     *
     * @param conversationId El ID de la conversación.
     * @param userId El ID del usuario que ha leído los mensajes.
     * @return Un Result que indica éxito (Unit) o un ChatError.
     */
    suspend fun markConversationAsRead(
        conversationId: String,
        userId: String
    ): Result<Unit, ChatError>

    /**
     * Obtiene una conversación específica por su ID.
     * Útil si solo necesitas los detalles de una conversación sin escuchar cambios.
     * Opcional, podría ser cubierto por getChatConversations si se filtra por ID.
     *
     * @param conversationId El ID de la conversación.
     * @return Un Result con la ChatConversation o un ChatError.
     */
    //suspend fun getConversationDetails(conversationId: String): Result<ChatConversation, ChatError>
}