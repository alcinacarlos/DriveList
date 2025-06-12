package com.carlosalcina.drivelist

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.CarSearchFilters
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.domain.repository.UserFavoriteRepository
import com.carlosalcina.drivelist.domain.usecase.ToggleFavoriteCarUseCase
import com.carlosalcina.drivelist.navigation.NavigationArgs
import com.carlosalcina.drivelist.ui.viewmodel.SearchVehicleScreenViewModel
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class SearchVehicleScreenViewModelTest {

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var carListRepository: CarListRepository
    private lateinit var userFavoriteRepository: UserFavoriteRepository
    private lateinit var uploadRepository: CarUploadRepository
    private lateinit var toggleFavoriteCarUseCase: ToggleFavoriteCarUseCase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var viewModel: SearchVehicleScreenViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val mockUser: FirebaseUser = mockk(relaxed = true)
    private val page1Results = listOf(CarForSale(id = "car1", brand = "Ford"))
    private val page2Results = listOf(CarForSale(id = "car2", brand = "Seat"))

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        Dispatchers.setMain(testDispatcher)
        savedStateHandle = mockk(relaxed = true)
        carListRepository = mockk(relaxed = true)
        userFavoriteRepository = mockk(relaxed = true)
        uploadRepository = mockk(relaxed = true)
        toggleFavoriteCarUseCase = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)

        every { mockUser.uid } returns "user123"
        every { savedStateHandle.get<String>(any()) } returns null
        every { firebaseAuth.currentUser } returns null
        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        every { firebaseAuth.addAuthStateListener(capture(listenerSlot)) } just runs
    }

    private fun createViewModel() {
        viewModel = SearchVehicleScreenViewModel(
            savedStateHandle, uploadRepository, carListRepository,
            userFavoriteRepository, toggleFavoriteCarUseCase, firebaseAuth
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init con filtros de navegacion aplica filtros y realiza busqueda inicial`() = runTest {
        val initialFilters = CarSearchFilters(searchTerm = "Focus")
        val initialFiltersJson = Gson().toJson(initialFilters)
        every { savedStateHandle.get<String>(NavigationArgs.SEARCH_FILTERS_JSON_ARG) } returns initialFiltersJson
        coEvery { carListRepository.searchCars(any(), any(), any()) } returns Result.Success(page1Results)

        createViewModel()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Focus", state.appliedFilters.searchTerm)
        assertEquals(page1Results, state.searchResults)
        assertFalse(state.isLoading)
    }

    @Test
    fun `onLoadMoreResults carga la siguiente pagina y anade los resultados`() = runTest {
        coEvery { carListRepository.searchCars(any(), 10, any()) } returns Result.Success(List(10) { page1Results[0] })
        createViewModel()
        testScheduler.advanceUntilIdle()

        coEvery { carListRepository.searchCars(any(), 10, any()) } returns Result.Success(page2Results)

        viewModel.onLoadMoreResults()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingMore)
        assertEquals(11, state.searchResults.size)
        assertEquals("Seat", state.searchResults.last().brand)
    }

    @Test
    fun `toggleFavoriteStatus con exito actualiza los IDs favoritos y la lista de resultados`() = runTest {
        val carToToggle = CarForSale(id = "car1", brand = "Ford", isFavoriteByCurrentUser = false)
        val carIdToToggle = "car1"
        val userId = "user123"

        coEvery { carListRepository.searchCars(any(), any(), any()) } returns Result.Success(listOf(carToToggle))
        coEvery { userFavoriteRepository.getUserFavoriteCarIds(userId) } returns Result.Success(emptyList())
        coEvery { toggleFavoriteCarUseCase(userId, carIdToToggle, false) } returns Result.Success(Unit)

        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        every { firebaseAuth.addAuthStateListener(capture(listenerSlot)) } just runs
        every { firebaseAuth.currentUser } returns null

        createViewModel()

        testScheduler.advanceUntilIdle()

        val mockAuthLoggedIn: FirebaseAuth = mockk { every { currentUser } returns mockUser }
        listenerSlot.captured.onAuthStateChanged(mockAuthLoggedIn)

        testScheduler.advanceUntilIdle()

        viewModel.toggleFavoriteStatus(carIdToToggle)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state.favoriteCarIds.contains(carIdToToggle), "El ID del coche deberia estar en la lista de favoritos")
        val updatedCarInList = state.searchResults.find { it.id == carIdToToggle }
        assertNotNull(updatedCarInList, "El coche deberia seguir en la lista de resultados")
        assertTrue(updatedCarInList.isFavoriteByCurrentUser, "El estado del coche en la lista deberia ser 'favorito'")
        assertNull(state.favoriteToggleError)
    }
}