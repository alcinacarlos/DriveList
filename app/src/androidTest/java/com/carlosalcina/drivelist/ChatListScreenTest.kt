package com.carlosalcina.drivelist

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import com.carlosalcina.drivelist.domain.model.ChatConversation
import com.carlosalcina.drivelist.navigation.Screen
import com.carlosalcina.drivelist.ui.states.ChatListUiState
import com.carlosalcina.drivelist.ui.theme.DriveListTheme
import com.carlosalcina.drivelist.ui.theme.ThemeOption
import com.carlosalcina.drivelist.ui.view.screens.ChatListScreen
import com.carlosalcina.drivelist.ui.viewmodel.ChatListViewModel
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

class ChatListScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var mockViewModel: ChatListViewModel
    private val mockNavController: NavController = mockk(relaxed = true)
    private lateinit var fakeUiStateFlow: MutableStateFlow<ChatListUiState>

    // Datos de prueba
    private val currentUserId = "user_me"
    private val sellerId = "user_seller"
    private val buyerId = "user_buyer"

    private val conversationWithUnread = ChatConversation(
        carId = "car1",
        carName = "Ford Fiesta",
        carImageUrl = "url_to_car_image",
        sellerId = sellerId,
        buyerId = currentUserId,
        sellerName = "Vendedor Ford",
        buyerName = "Comprador Yo",
        lastMessageText = "Hola, me interesa.",
        lastMessageTimestamp = Timestamp(Date()),
        unreadCount = mapOf(currentUserId to 2)
    )

    private val conversationSentByMe = ChatConversation(
        carId = "car2",
        carName = "Seat Ibiza",
        carImageUrl = "url_to_another_car",
        sellerId = sellerId,
        buyerId = currentUserId,
        sellerName = "Vendedor Seat",
        buyerName = "Comprador Yo",
        lastMessageText = "¿Sigue disponible?",
        lastMessageTimestamp = Timestamp(Date()),
        lastMessageSenderId = currentUserId,
        unreadCount = mapOf(sellerId to 0)
    )

    @Before
    fun setUp() {
        fakeUiStateFlow = MutableStateFlow(ChatListUiState())
        mockViewModel = mockk(relaxed = true) {
            every { uiState } returns fakeUiStateFlow
        }
    }

    private fun setContent() {
        composeTestRule.setContent {
            DriveListTheme(appTheme = ThemeOption.DARK) {
                ChatListScreen(
                    navController = mockNavController,
                    viewModel = mockViewModel
                )
            }
        }
    }

    // Tests de Estados

    @Test
    fun cuandoEstadoEsLoading_seMuestraIndicadorDeProgreso() {
        fakeUiStateFlow.value = ChatListUiState(isLoading = true)

        setContent()

        composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()

    }

    @Test
    fun cuandoHayError_seMuestraMensajeYBotonReintentar() {
        val errorMsg = "Fallo de red"
        fakeUiStateFlow.value = ChatListUiState(error = errorMsg)

        setContent()

        composeTestRule.onNodeWithText("Error: $errorMsg").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }

    @Test
    fun alPulsarReintentar_seLlamaRefreshConversations() {
        fakeUiStateFlow.value = ChatListUiState(error = "Error")
        setContent()

        composeTestRule.onNodeWithText("Reintentar").performClick()

        verify { mockViewModel.refreshConversations() }
    }

    @Test
    fun cuandoNoHayConversaciones_seMuestraEstadoVacio() {
        fakeUiStateFlow.value = ChatListUiState(isLoading = false, error = null, conversations = emptyList())

        setContent()

        composeTestRule.onNodeWithText("No tienes conversaciones").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("No hay chats").assertIsDisplayed()
    }

    // Tests de la Lista de Conversaciones

    @Test
    fun cuandoHayConversaciones_seMuestraLaLista() {
        fakeUiStateFlow.value = ChatListUiState(
            conversations = listOf(conversationWithUnread, conversationSentByMe),
            currentUserId = currentUserId
        )

        setContent()

        composeTestRule.onNodeWithText("Vendedor Ford").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hola, me interesa.").assertIsDisplayed()

        composeTestRule.onNodeWithText("Vendedor Seat").assertIsDisplayed()
        composeTestRule.onNodeWithText("¿Sigue disponible?").assertIsDisplayed()
    }

    @Test
    fun conversacionConNoLeidos_muestraBadgeConContador() {
        fakeUiStateFlow.value = ChatListUiState(
            conversations = listOf(conversationWithUnread),
            currentUserId = currentUserId
        )
        setContent()

        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }

    @Test
    fun mensajeEnviadoPorMiYLeido_muestraIconoDeLeido() {
        fakeUiStateFlow.value = ChatListUiState(
            conversations = listOf(conversationSentByMe),
            currentUserId = currentUserId
        )
        setContent()

        composeTestRule.onNodeWithContentDescription("Leído").assertIsDisplayed()
    }

    // Tests de Navegación

    @Test
    fun alPulsarConversacion_seNavegaADetalleDeChat() {
        fakeUiStateFlow.value = ChatListUiState(
            conversations = listOf(conversationWithUnread),
            currentUserId = currentUserId
        )
        setContent()

        composeTestRule.onNodeWithText("Vendedor Ford").performClick()

        verify {
            mockNavController.navigate(
                Screen.ChatDetail.createRoute(
                    carId = conversationWithUnread.carId!!,
                    sellerId = conversationWithUnread.sellerId,
                    buyerId = conversationWithUnread.buyerId
                )
            )
        }
    }

    @Test
    fun alPulsarImagenDelCoche_seNavegaADetalleDeCoche() {
        fakeUiStateFlow.value = ChatListUiState(
            conversations = listOf(conversationWithUnread),
            currentUserId = currentUserId
        )
        setContent()

        composeTestRule.onNodeWithContentDescription("Avatar de ${conversationWithUnread.sellerName}").performClick()

        verify {
            mockNavController.navigate(
                Screen.CarDetail.createRoute(conversationWithUnread.carId!!)
            )
        }
    }
}