package com.carlosalcina.drivelist.data.repository

import com.carlosalcina.drivelist.domain.error.ChatError
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.ChatConversation
import com.carlosalcina.drivelist.domain.model.ChatMessage
import com.carlosalcina.drivelist.domain.model.UserData
import com.carlosalcina.drivelist.domain.repository.ChatRepository
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ChatRepository {

    companion object {
        private const val CONVERSATIONS_COLLECTION = "conversaciones"
        private const val MESSAGES_SUBCOLLECTION = "mensajes"
    }

    override suspend fun createOrGetConversation(
        currentUser: UserData,
        otherUser: UserData,
        car: CarForSale
    ): Result<String, ChatError> {
        // Generar un ID de conversación predecible para evitar duplicados
        // Ordenar los UIDs para que el ID sea el mismo independientemente de quién inicie
        val participantUids = listOf(currentUser.uid, otherUser.uid).sorted()
        val conversationId = "${participantUids[0]}_${participantUids[1]}_${car.id}"

        val conversationRef = firestore.collection(CONVERSATIONS_COLLECTION).document(conversationId)

        return try {
            val snapshot = conversationRef.get().await()
            if (snapshot.exists()) {
                // La conversación ya existe
                Result.Success(conversationId)
            } else {
                // La conversación no existe, la creamos
                val newConversation = ChatConversation(
                    id = conversationId,
                    carId = car.id,
                    carImageUrl = car.imageUrls.firstOrNull(),
                    carName = "${car.brand} ${car.model}", // O una representación más detallada
                    buyerId = if (currentUser.uid == car.userId) otherUser.uid else currentUser.uid, // Asumiendo que el dueño del coche es el vendedor
                    buyerName = if (currentUser.uid == car.userId) otherUser.displayName else currentUser.displayName,
                    buyerPhotoUrl = if (currentUser.uid == car.userId) otherUser.photoURL else currentUser.photoURL,
                    sellerId = if (currentUser.uid == car.userId) currentUser.uid else otherUser.uid,
                    sellerName = if (currentUser.uid == car.userId) currentUser.displayName else otherUser.displayName,
                    sellerPhotoUrl = if (currentUser.uid == car.userId) currentUser.photoURL else otherUser.photoURL,
                    participantIds = participantUids,
                    lastMessageText = null,
                    lastMessageTimestamp = null, // Se actualizará con el primer mensaje
                    lastMessageSenderId = null,
                    unreadCount = mapOf(currentUser.uid to 0, otherUser.uid to 0) // Inicializar contadores
                )
                conversationRef.set(newConversation).await()
                Result.Success(conversationId)
            }
        } catch (e: Exception) {
            Result.Error(ChatError.OperationFailed("No se pudo crear o acceder a la conversación: ${e.message}"))
        }
    }


    override fun getChatConversations(userId: String): Flow<Result<List<ChatConversation>, ChatError>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(Result.Error(ChatError.UserNotAuthenticated("ID de usuario no válido.")))
            close()
            return@callbackFlow
        }

        val query = firestore.collection(CONVERSATIONS_COLLECTION)
            .whereArrayContains("participantIds", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        val listenerRegistration = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                trySend(Result.Error(ChatError.NetworkError("Error de red al obtener conversaciones.")))
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val conversations = snapshots.toObjects<ChatConversation>()
                trySend(Result.Success(conversations))
            } else {
                trySend(Result.Success(emptyList())) // Enviar lista vacía si no hay snapshots
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun markAllMessagesAsReadInConversation(
        conversationId: String,
        readerId: String
    ): Result<Unit, ChatError> {
        if (conversationId.isEmpty() || readerId.isEmpty()) {
            return Result.Error(ChatError.OperationFailed("IDs inválidos para marcar mensajes como leídos."))
        }

        val messagesRef = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
            .collection(MESSAGES_SUBCOLLECTION)

        return try {
            // 1. Encontrar todos los mensajes no leídos que fueron enviados a este usuario
            val unreadMessagesQuery = messagesRef
                .whereEqualTo("receiverId", readerId)
                .whereEqualTo("messageReaded", false)
                .get()
                .await()

            if (unreadMessagesQuery.isEmpty) {
                return Result.Success(Unit)
            }

            val batch = firestore.batch()
            for (document in unreadMessagesQuery.documents) {
                batch.update(document.reference, "messageReaded", true)
            }

            batch.commit().await()
            Result.Success(Unit)

        } catch (e: Exception) {
            Result.Error(ChatError.OperationFailed("No se pudieron actualizar los mensajes como leídos: ${e.message}"))
        }
    }


    override fun getMessages(conversationId: String): Flow<Result<List<ChatMessage>, ChatError>> = callbackFlow {
        if (conversationId.isEmpty()) {
            trySend(Result.Error(ChatError.ConversationNotFound("ID de conversación no válido.")))
            close()
            return@callbackFlow
        }

        val query = firestore.collection(CONVERSATIONS_COLLECTION).document(conversationId)
            .collection(MESSAGES_SUBCOLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(50)

        val listenerRegistration = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                trySend(Result.Error(ChatError.NetworkError("Error de red al obtener mensajes.")))
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val messages = snapshots.toObjects<ChatMessage>()
                trySend(Result.Success(messages))
            } else {
                trySend(Result.Success(emptyList()))
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        senderName: String?,
        senderPhotoUrl: String?,
        receiverId: String,
        text: String,
        imageUrl: String?
    ): Result<Unit, ChatError> {
        if (conversationId.isEmpty() || senderId.isEmpty() || receiverId.isEmpty()) {
            return Result.Error(ChatError.OperationFailed("IDs inválidos para enviar mensaje."))
        }
        if (text.isBlank() && imageUrl == null) {
            return Result.Error(ChatError.MessageSendError("El mensaje no puede estar vacío."))
        }

        val conversationRef = firestore.collection(CONVERSATIONS_COLLECTION).document(conversationId)
        val messageRef = conversationRef.collection(MESSAGES_SUBCOLLECTION).document() // Nuevo ID de mensaje

        val newMessage = ChatMessage(
            id = messageRef.id,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            senderPhotoUrl = senderPhotoUrl,
            receiverId = receiverId,
            text = text,
            timestamp = null, // Firestore asignará esto con @ServerTimestamp en el modelo
            messageReaded = false, // El mensaje es nuevo, por lo tanto no leído
            imageUrl = imageUrl
        )

        return try {
            firestore.runBatch { batch ->
                // 1. Añadir el nuevo mensaje
                batch.set(messageRef, newMessage)

                // 2. Actualizar la conversación con el último mensaje y el contador de no leídos
                val conversationUpdateData = hashMapOf(
                    "lastMessageText" to if (imageUrl != null && text.isBlank()) "Imagen" else text,
                    "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                    "lastMessageSenderId" to senderId,
                    "unreadCount.$receiverId" to FieldValue.increment(1) // Incrementar contador para el receptor
                )
                batch.update(conversationRef, conversationUpdateData)
            }.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ChatError.MessageSendError("No se pudo enviar el mensaje: ${e.message}"))
        }
    }

    override suspend fun markConversationAsRead(
        conversationId: String,
        userId: String
    ): Result<Unit, ChatError> {
        if (conversationId.isEmpty() || userId.isEmpty()) {
            return Result.Error(ChatError.OperationFailed("IDs inválidos para marcar como leído."))
        }

        val conversationRef = firestore.collection(CONVERSATIONS_COLLECTION).document(conversationId)

        return try {
            // Actualizar el contador de no leídos para el usuario actual a 0
            // Usamos SetOptions.merge() si el campo puede no existir aún, aunque lo inicializamos.
            // O directamente actualizamos el campo específico.
            val updateData = mapOf("unreadCount.$userId" to 0)
            conversationRef.update(updateData).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ChatError.OperationFailed("No se pudo marcar la conversación como leída: ${e.message}"))
        }
    }
}