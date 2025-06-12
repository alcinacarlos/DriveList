package com.carlosalcina.drivelist

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.QuickFilter
import com.carlosalcina.drivelist.domain.model.QuickFilterType
import com.carlosalcina.drivelist.ui.navigation.Screen
import com.carlosalcina.drivelist.ui.states.SearchVehicleScreenState
import com.carlosalcina.drivelist.ui.theme.DriveListTheme
import com.carlosalcina.drivelist.ui.theme.ThemeOption
import com.carlosalcina.drivelist.ui.view.screens.SearchVehicleScreen
import com.carlosalcina.drivelist.ui.viewmodel.SearchVehicleScreenViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchVehicleScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var mockViewModel: SearchVehicleScreenViewModel
    private val mockNavController: NavController = mockk(relaxed = true)
    private lateinit var fakeUiStateFlow: MutableStateFlow<SearchVehicleScreenState>

    // --- Datos de prueba ---
    private val testCars = listOf(
        CarForSale(id = "car1", brand = "Ford", model = "Focus"),
        CarForSale(id = "car2", brand = "Seat", model = "Ibiza")
    )
    private val testQuickFilter = QuickFilter(
        id = "fuel_diesel",
        type = QuickFilterType.FUEL_TYPE,
        displayText = "Diesel",
        value = "Diesel"
    )

    @Before
    fun setUp() {
        fakeUiStateFlow = MutableStateFlow(SearchVehicleScreenState())
        mockViewModel = mockk(relaxed = true) {
            every { uiState } returns fakeUiStateFlow
        }
    }

    private fun renderScreen() {
        composeTestRule.setContent {
            DriveListTheme(appTheme = ThemeOption.DARK) {
                SearchVehicleScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
    }

    @Test
    fun cuandoIsLoadingEsTrueYNoHayResultados_seMuestraCircularProgress() {
        fakeUiStateFlow.value = SearchVehicleScreenState(isLoading = true, searchResults = emptyList())

        renderScreen()

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun cuandoNoResultsFoundEsTrue_seMuestraMensajeDeNoResultados() {
        fakeUiStateFlow.value = SearchVehicleScreenState(noResultsFound = true, searchResults = emptyList())

        renderScreen()

        composeTestRule.onNodeWithText("No se encontraron coches con los filtros actuales. Prueba a modificarlos.").assertIsDisplayed()
    }

    @Test
    fun cuandoHayResultados_seMuestraLaListaDeCoches() {
        fakeUiStateFlow.value = SearchVehicleScreenState(searchResults = testCars)

        renderScreen()

        composeTestRule.onNodeWithText("Ford Focus").assertIsDisplayed()
        composeTestRule.onNodeWithText("Seat Ibiza").assertIsDisplayed()
    }

    @Test
    fun alPulsarUnFiltroRapido_seLlamaAOnApplyQuickFilter() {
        fakeUiStateFlow.value = SearchVehicleScreenState(quickFilters = listOf(testQuickFilter))
        renderScreen()

        composeTestRule.onNodeWithText(testQuickFilter.displayText).performClick()

        verify { mockViewModel.onApplyQuickFilter(testQuickFilter) }
    }

    @Test
    fun alPulsarElBotonDeFiltros_seAbreElDialogoDeFiltrosAvanzados() {
        renderScreen()

        composeTestRule.onNodeWithContentDescription("Abrir filtros avanzados").performClick()

        verify { mockViewModel.openAdvancedFiltersDialog() }
    }


    @Test
    fun alPulsarUnCocheEnLaLista_seNavegaASuDetalle() {
        fakeUiStateFlow.value = SearchVehicleScreenState(searchResults = testCars)
        renderScreen()
        val carToClick = testCars.first()

        composeTestRule.onNodeWithText("${carToClick.brand} ${carToClick.model}").performClick()

        verify { mockNavController.navigate(Screen.CarDetail.createRoute(carToClick.id)) }
    }
}