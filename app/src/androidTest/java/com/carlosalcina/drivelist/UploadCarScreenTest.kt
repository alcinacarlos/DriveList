package com.carlosalcina.drivelist

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import com.carlosalcina.drivelist.ui.states.UploadCarScreenState
import com.carlosalcina.drivelist.ui.theme.DriveListTheme
import com.carlosalcina.drivelist.ui.theme.ThemeOption
import com.carlosalcina.drivelist.ui.view.screens.UploadCarScreen
import com.carlosalcina.drivelist.ui.viewmodel.UploadCarViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UploadCarScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var mockViewModel: UploadCarViewModel
    private val mockNavController: NavController = mockk(relaxed = true)
    private val onUploadSuccess: () -> Unit = mockk(relaxed = true)

    private lateinit var fakeUiStateFlow: MutableStateFlow<UploadCarScreenState>

    @Before
    fun setUp() {
        fakeUiStateFlow = MutableStateFlow(UploadCarScreenState())
        mockViewModel = mockk(relaxed = true) {
            every { uiState } returns fakeUiStateFlow
        }
    }

    private fun renderScreen() {
        composeTestRule.setContent {
            DriveListTheme(appTheme = ThemeOption.DARK) {
                UploadCarScreen(
                    viewModel = mockViewModel,
                    onUploadSuccess = onUploadSuccess,
                    navController = mockNavController
                )
            }
        }
    }

    @Test
    fun cuandoLosCamposRequeridosNoEstanCompletos_elBotonDeSubirEstaDeshabilitado() {
        fakeUiStateFlow.value = UploadCarScreenState(
            selectedBrand = "Ford",
            selectedModel = "Focus",
            selectedBodyType = "Hatchback",
            selectedFuelType = "Gasoline",
            selectedYear = "2020",
            selectedVersion = null,
            finalPostalCode = "28001",
            description = "Descripcion",
            price = "15000",
            mileage = "50000"
        )

        // When
        renderScreen()

        // Then
        composeTestRule.onNodeWithText("Subir Coche").assertIsNotEnabled()
    }

    @Test
    fun cuandoTodosLosCamposRequeridosEstanCompletos_elBotonDeSubirEstaHabilitado() {
        fakeUiStateFlow.value = UploadCarScreenState(
            selectedBrand = "Ford",
            selectedModel = "Focus",
            selectedBodyType = "Hatchback",
            selectedFuelType = "Gasoline",
            selectedYear = "2020",
            selectedVersion = "ST",
            finalPostalCode = "28001",
            description = "Descripcion",
            price = "15000",
            mileage = "50000"
        )

        renderScreen()

        composeTestRule.onNodeWithText("Subir Coche").assertIsEnabled()
    }


    @Test
    fun cuandoSeEscribeEnElPrecio_seLlamaAOnPriceChanged() {
        renderScreen()
        val priceInput = "20000"

        // When
        composeTestRule.onNodeWithText("Precio (€)").performTextInput(priceInput)

        verify { mockViewModel.onPriceChanged(priceInput) }
    }

    @Test
    fun cuandoSeMuestraUnaImagenSeleccionada_elBotonDeBorrarEsVisible() {
        val mockUri = mockk<Uri>()
        fakeUiStateFlow.value = UploadCarScreenState(selectedImageUris = listOf(mockUri))
        renderScreen()

        composeTestRule.onNodeWithContentDescription("Quitar imagen").performClick()

        verify { mockViewModel.removeSelectedImage(mockUri) }
    }

}