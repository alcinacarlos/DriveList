package com.carlosalcina.drivelist

import app.cash.turbine.test
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.domain.repository.UserFavoriteRepository
import com.carlosalcina.drivelist.domain.usecase.ToggleFavoriteCarUseCase
import com.carlosalcina.drivelist.ui.viewmodel.FavoritesViewModel
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class FavoritesViewModelTest {

    private lateinit var userFavoriteRepository: UserFavoriteRepository
    private lateinit var carListRepository: CarListRepository
    private lateinit var toggleFavoriteCarUseCase: ToggleFavoriteCarUseCase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var viewModel: FavoritesViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val mockUser: FirebaseUser = mockk(relaxed = true)
    private val favoriteCarIds = listOf("car1", "car2")
    private val mockFavoriteCars = listOf(
        CarForSale(id = "car1", brand = "Ford"),
        CarForSale(id = "car2", brand = "Seat")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userFavoriteRepository = mockk()
        carListRepository = mockk()
        toggleFavoriteCarUseCase = mockk()
        firebaseAuth = mockk(relaxed = true)

        every { mockUser.uid } returns "user123"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `cargar favoritos con fallo parcial carga los coches exitosos y muestra error`() = runTest {
        every { firebaseAuth.currentUser } returns mockUser
        coEvery { userFavoriteRepository.getUserFavoriteCarIds("user123") } returns Result.Success(favoriteCarIds)
        coEvery { carListRepository.getCarById("car1", "user123") } returns Result.Success(mockFavoriteCars[0])
        coEvery { carListRepository.getCarById("car2", "user123") } returns Result.Error(Exception("Coche no encontrado"))

        viewModel = FavoritesViewModel(
            userFavoriteRepository, carListRepository, toggleFavoriteCarUseCase, firebaseAuth
        )

        viewModel.uiState.test {
            skipItems(2)

            val partialSuccessState = awaitItem()
            assertFalse(partialSuccessState.isLoading)
            assertEquals("Error al cargar detalles de algunos favoritos.", partialSuccessState.error)
            assertEquals(1, partialSuccessState.favoriteCars.size)
            assertEquals("Ford", partialSuccessState.favoriteCars[0].brand)
        }
    }

    @Test
    fun `cargar favoritos con exito actualiza la lista de coches`() = runTest {
        every { firebaseAuth.currentUser } returns mockUser
        coEvery { userFavoriteRepository.getUserFavoriteCarIds("user123") } returns Result.Success(favoriteCarIds)
        coEvery { carListRepository.getCarById("car1", "user123") } returns Result.Success(mockFavoriteCars[0])
        coEvery { carListRepository.getCarById("car2", "user123") } returns Result.Success(mockFavoriteCars[1])

        viewModel = FavoritesViewModel(
            userFavoriteRepository, carListRepository, toggleFavoriteCarUseCase, firebaseAuth
        )

        viewModel.uiState.test {
            skipItems(2)

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertNull(successState.error)
            assertEquals(2, successState.favoriteCars.size)
        }
    }


    @Test
    fun `toggleFavoriteStatus con exito elimina el coche de la lista`() = runTest {
        every { firebaseAuth.currentUser } returns mockUser
        coEvery { userFavoriteRepository.getUserFavoriteCarIds(any()) } returns Result.Success(favoriteCarIds)
        coEvery { carListRepository.getCarById("car1", any()) } returns Result.Success(mockFavoriteCars[0])
        coEvery { carListRepository.getCarById("car2", any()) } returns Result.Success(mockFavoriteCars[1])

        viewModel = FavoritesViewModel(
            userFavoriteRepository, carListRepository, toggleFavoriteCarUseCase, firebaseAuth
        )

        viewModel.uiState.test {
            skipItems(2)
            val initialState = awaitItem()
            assertEquals(2, initialState.favoriteCars.size)

            val carToToggleId = "car1"
            coEvery { toggleFavoriteCarUseCase(any(), carToToggleId, any()) } returns Result.Success(Unit)

            viewModel.toggleFavoriteStatus(carToToggleId)

            val togglingState = awaitItem()
            assertTrue(togglingState.isTogglingFavorite[carToToggleId] == true)

            val finalState = awaitItem()
            assertNull(finalState.isTogglingFavorite[carToToggleId])
            assertEquals(1, finalState.favoriteCars.size)
            assertNull(finalState.favoriteCars.find { it.id == carToToggleId })
        }
    }

    @Test
    fun `toggleFavoriteStatus con fallo muestra error y no elimina el coche`() = runTest {
        every { firebaseAuth.currentUser } returns mockUser
        coEvery { userFavoriteRepository.getUserFavoriteCarIds(any()) } returns Result.Success(favoriteCarIds)
        coEvery { carListRepository.getCarById(any(), any()) } returns Result.Success(mockFavoriteCars[0]) andThen Result.Success(mockFavoriteCars[1])

        viewModel = FavoritesViewModel(
            userFavoriteRepository, carListRepository, toggleFavoriteCarUseCase, firebaseAuth
        )

        viewModel.uiState.test {
            skipItems(3)

            val carToToggleId = "car1"
            val errorMessage = "Fallo al quitar favorito"
            coEvery { toggleFavoriteCarUseCase(any(), carToToggleId, any()) } returns Result.Error(Exception(errorMessage))

            viewModel.toggleFavoriteStatus(carToToggleId)

            val togglingState = awaitItem()
            assertTrue(togglingState.isTogglingFavorite[carToToggleId] == true)

            val errorState = awaitItem()
            assertNull(errorState.isTogglingFavorite[carToToggleId])
            assertEquals(errorMessage, errorState.error)
            assertEquals(2, errorState.favoriteCars.size)
        }
    }
}