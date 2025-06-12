package com.carlosalcina.drivelist

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.ChatMessage
import com.carlosalcina.drivelist.domain.model.UserData
import com.carlosalcina.drivelist.ui.navigation.Screen
import com.carlosalcina.drivelist.ui.states.ChatDetailUiState
import com.carlosalcina.drivelist.ui.theme.DriveListTheme
import com.carlosalcina.drivelist.ui.theme.ThemeOption
import com.carlosalcina.drivelist.ui.view.screens.ChatDetailScreen
import com.carlosalcina.drivelist.ui.viewmodel.ChatDetailViewModel
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

class ChatDetailScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // Mocks para las dependencias de la pantalla
    private lateinit var mockViewModel: ChatDetailViewModel
    private val mockNavController: NavController = mockk(relaxed = true)

    // Flujo de estado falso que controlaremos en cada prueba
    private lateinit var fakeUiStateFlow: MutableStateFlow<ChatDetailUiState>

    // Datos de prueba reutilizables
    private val currentUserId = "currentUser123"
    private val otherUserId = "otherUser456"

    private val otherParticipant = UserData(uid = otherUserId, displayName = "Jane Doe")
    private val carDetails = CarForSale(brand = "Renault", model = "Clio", imageUrls = listOf("car_photo_url"))

    private val testMessages = listOf(
        ChatMessage(senderId = otherUserId, text = "Hola, ¿sigue disponible?", timestamp = Timestamp(Date(System.currentTimeMillis() - 20000))),
        ChatMessage(senderId = currentUserId, text = "Sí, todavía lo está.", timestamp = Timestamp(Date(System.currentTimeMillis() - 10000)), messageReaded = true)
    )

    @Before
    fun setUp() {
        // Inicializamos el estado y el ViewModel mockeado antes de cada test
        fakeUiStateFlow = MutableStateFlow(ChatDetailUiState())
        mockViewModel = mockk(relaxed = true) {
            every { uiState } returns fakeUiStateFlow
        }
    }

    private fun setContent() {
        composeTestRule.setContent {
            DriveListTheme(appTheme = ThemeOption.DARK) {
                ChatDetailScreen(
                    navController = mockNavController,
                    viewModel = mockViewModel
                )
            }
        }
    }

    // Tests de Estados Visuales

    @Test
    fun cuandoEstadoEsLoading_seMuestraCircularProgressIndicator() {
        fakeUiStateFlow.value = ChatDetailUiState(isLoadingInitialData = true)

        setContent()

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }


    // Tests de Interacciones en la TopAppBar

    @Test
    fun topAppBar_muestraNombreYDatosDelCocheCorrectamente() {
        fakeUiStateFlow.value = ChatDetailUiState(
            otherParticipant = otherParticipant,
            carDetails = carDetails
        )

        setContent()

        composeTestRule.onNodeWithText("Jane Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sobre: Renault Clio").assertIsDisplayed()
    }

    @Test
    fun alPulsarBotonAtras_seInvocaNavPopBackStack() {
        setContent()

        composeTestRule.onNodeWithContentDescription("Atrás").performClick()

        verify { mockNavController.popBackStack() }
    }

    @Test
    fun alPulsarInfoUsuarioEnTopBar_seNavegaAlPerfil() {
        fakeUiStateFlow.value = ChatDetailUiState(
            otherParticipant = otherParticipant
        )
        setContent()

        composeTestRule.onNodeWithText(otherParticipant.displayName!!).performClick()

        verify { mockNavController.navigate(Screen.Profile.createRoute(otherUserId)) }
    }

    // Tests de Interacciones en la Barra de Mensajes

    @Test
    fun escribirEnTextField_invocaOnMessageTextChanged() {
        setContent()
        val typedMessage = "Hola mundo"

        composeTestRule.onNodeWithText("Escribe un mensaje...").performTextInput(typedMessage)

        verify { mockViewModel.onMessageTextChanged(typedMessage) }
    }

    @Test
    fun botonEnviar_estaDeshabilitadoCuandoElMensajeEstaVacio() {
        fakeUiStateFlow.value = ChatDetailUiState(
            canSendMessage = true,
            currentMessageText = ""
        )
        setContent()

        composeTestRule.onNodeWithContentDescription("Enviar mensaje").assertIsNotEnabled()
    }

    @Test
    fun botonEnviar_estaHabilitadoCuandoHayTexto() {
        fakeUiStateFlow.value = ChatDetailUiState(
            canSendMessage = true,
            currentMessageText = "Un mensaje"
        )
        setContent()

        composeTestRule.onNodeWithContentDescription("Enviar mensaje").assertIsEnabled()
    }

    @Test
    fun alPulsarEnviar_seInvocaSendMessage() {
        fakeUiStateFlow.value = ChatDetailUiState(
            canSendMessage = true,
            currentMessageText = "Un mensaje para enviar"
        )
        setContent()

        composeTestRule.onNodeWithContentDescription("Enviar mensaje").performClick()

        verify { mockViewModel.sendMessage() }
    }
}