package com.carlosalcina.drivelist

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import com.carlosalcina.drivelist.navigation.Screen
import com.carlosalcina.drivelist.ui.theme.DriveListTheme
import com.carlosalcina.drivelist.ui.theme.ThemeOption
import com.carlosalcina.drivelist.ui.view.components.TopBar
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TopBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var mockNavController: NavController

    @Before
    fun setUp() {
        mockNavController = mockk(relaxed = true)
    }

    private fun renderTopBar(showBackArrow: Boolean = false) {
        composeTestRule.setContent {
            DriveListTheme(appTheme = ThemeOption.DARK) {
                TopBar(
                    navController = mockNavController,
                    stringResource = R.string.app_name,
                    showBackArrow = showBackArrow
                )
            }
        }
    }

    @Test
    fun topBar_muestraElTituloCorrectamente() {
        renderTopBar()
        val expectedTitle = composeTestRule.activity.getString(R.string.app_name)

        composeTestRule.onNodeWithText(expectedTitle).assertIsDisplayed()
    }

    @Test
    fun cuandoShowBackArrowEsFalse_elBotonAtrasNoSeMuestra() {
        renderTopBar(showBackArrow = false)

        composeTestRule.onNodeWithContentDescription("Back").assertDoesNotExist()
    }

    @Test
    fun cuandoShowBackArrowEsTrue_elBotonAtrasSeMuestra() {
        renderTopBar(showBackArrow = true)

        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun alPulsarElBotonAtras_seLlamaNavPopBackStack() {
        renderTopBar(showBackArrow = true)

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        verify(exactly = 1) { mockNavController.popBackStack() }
    }

    @Test
    fun alPulsarElIconoDePerfil_seNavegaAPerfil() {
        renderTopBar()

        composeTestRule.onNodeWithContentDescription("Profile").performClick()

        verify(exactly = 1) { mockNavController.navigate(Screen.Profile.route) }
    }


    @Test
    fun alPulsarAjustesEnElMenu_seNavegaAAjustes() {
        renderTopBar()
        composeTestRule.onNodeWithContentDescription("Menu").performClick() // Abrimos el menu

        composeTestRule.onNodeWithText("Ajustes").performClick()

        verify(exactly = 1) { mockNavController.navigate(Screen.Settings.route) }
        composeTestRule.onNodeWithText("Ajustes").assertDoesNotExist() // El menu ya no es visible
    }

    @Test
    fun alPulsarSobreLaAppEnElMenu_seNavegaAAbout() {
        renderTopBar()
        composeTestRule.onNodeWithContentDescription("Menu").performClick()

        composeTestRule.onNodeWithText("Sobre la app").performClick()

        verify(exactly = 1) { mockNavController.navigate(Screen.About.route) }
    }

}