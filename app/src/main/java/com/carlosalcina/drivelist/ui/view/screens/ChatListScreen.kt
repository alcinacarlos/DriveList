package com.carlosalcina.drivelist.ui.view.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.carlosalcina.drivelist.R
import com.carlosalcina.drivelist.domain.model.ChatConversation
import com.carlosalcina.drivelist.navigation.Screen
import com.carlosalcina.drivelist.ui.view.components.AppBottomNavigationBar
import com.carlosalcina.drivelist.ui.view.components.TopBar
import com.carlosalcina.drivelist.ui.viewmodel.ChatListViewModel
import com.carlosalcina.drivelist.utils.Utils.formatTimestampForChatList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController, viewModel: ChatListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopBar(navController, stringResource = R.string.screen_title_chat_detail)
        },
        bottomBar = {
            AppBottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.refreshConversations() }) {
                        Text("Reintentar")
                    }
                }
            } else if (uiState.conversations.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.ChatBubbleOutline,
                        contentDescription = "No hay chats",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "No tienes conversaciones",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        "Inicia un chat con un vendedor para verlo aquí.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.conversations.size) { conversation ->
                        ChatConversationItem(
                            conversation = uiState.conversations[conversation],
                            currentUserId = uiState.currentUserId ?: "",
                            onCarClick = {
                                navController.navigate(Screen.CarDetail.createRoute(uiState.conversations[conversation].carId!!))
                            },
                            onConversationClick = { conv ->
                                if (conv.carId != null) {
                                    navController.navigate(
                                        Screen.ChatDetail.createRoute(
                                            carId = conv.carId,
                                            sellerId = conv.sellerId,
                                            buyerId = conv.buyerId
                                        )
                                    )
                                }
                            })
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ChatConversationItem(
    conversation: ChatConversation,
    currentUserId: String,
    onConversationClick: (ChatConversation) -> Unit,
    onCarClick: () -> Unit
) {
    // Determinar quién es el otro participante para mostrar su info
    val otherParticipantName: String?
    val otherId: String?

    if (currentUserId == conversation.buyerId) {
        otherParticipantName = conversation.sellerName
        otherId = conversation.sellerId
    } else {
        otherParticipantName = conversation.buyerName
        otherId = conversation.buyerId
    }

    val unreadCount = conversation.unreadCount[currentUserId] ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onConversationClick(conversation) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model =  conversation.carImageUrl,
            contentDescription = "Avatar de $otherParticipantName",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .clickable{ onCarClick() },
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_avatar_placeholder),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = otherParticipantName ?: conversation.carName ?: "Chat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.align(Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.lastMessageText ?: "No hay mensajes aún.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (conversation.lastMessageSenderId == currentUserId) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (conversation.unreadCount[otherId] == 0) Icons.Default.DoneAll else Icons.Default.Done,
                        contentDescription = if (conversation.unreadCount[otherId] == 0) "Leído" else "Enviado",
                        tint = if (conversation.unreadCount[otherId] == 0) Color.Blue else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (conversation.carName != null && otherParticipantName != conversation.carName) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Sobre: ${conversation.carName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.height(IntrinsicSize.Min) // Para alinear bien el badge si hay mucho texto
        ) {
            Text(
                text = formatTimestampForChatList(conversation.lastMessageTimestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (unreadCount > 0) {
                Spacer(modifier = Modifier.height(6.dp)) // Espacio antes del badge
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(top = 0.dp) // Ajusta si es necesario
                ) {
                    Text(
                        text = unreadCount.toString(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp)) // Para mantener la altura si no hay badge
            }
        }
    }
}