package com.carlosalcina.drivelist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.UserData
import com.carlosalcina.drivelist.domain.repository.AuthRepository
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.ui.navigation.NavigationArgs
import com.carlosalcina.drivelist.ui.states.CarDataState
import com.carlosalcina.drivelist.ui.states.SellerUiState
import com.carlosalcina.drivelist.ui.viewmodel.CarDetailViewModel
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
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

@ExperimentalCoroutinesApi
class CarDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var carListRepository: CarListRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: CarDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockCar = CarForSale(id = "car1", userId = "seller123", brand = "Test", model = "Car")
    private val mockSeller = UserData(uid = "seller123", displayName = "Test Seller")
    private val mockCurrentUser = mockk<FirebaseUser>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        carListRepository = mockk()
        authRepository = mockk()
        firebaseAuth = mockk()
        savedStateHandle = mockk(relaxed = true)

        every { savedStateHandle.get<String>(NavigationArgs.CAR_ID_ARG) } returns "car1"
        every { firebaseAuth.currentUser } returns mockCurrentUser
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `loadCarDetails - exito al cargar coche y vendedor`() = runTest {
        coEvery { carListRepository.getCarById("car1", "buyer456") } returns Result.Success(mockCar)
        coEvery { authRepository.getUserData("seller123") } returns Result.Success(mockSeller)
        every { mockCurrentUser.uid } returns "buyer456"

        viewModel = CarDetailViewModel(carListRepository, authRepository, firebaseAuth, savedStateHandle)

        viewModel.uiState.test {
            assertEquals(CarDataState.Loading, awaitItem().carDataState)

            val successState = awaitItem()
            assertIs<CarDataState.Success>(successState.carDataState)
            assertEquals(mockCar, successState.carDataState.car)
            assertIs<SellerUiState.Success>(successState.sellerUiState)
            assertEquals(mockSeller, successState.sellerUiState.userData)
        }
    }

    @Test
    fun `loadCarDetails - cuando el usuario actual es el vendedor, isBuyer es falso`() = runTest {
        coEvery { carListRepository.getCarById(any(), any()) } returns Result.Success(mockCar)
        coEvery { authRepository.getUserData(any()) } returns Result.Success(mockSeller)
        every { mockCurrentUser.uid } returns "seller123" // Current user is the seller

        viewModel = CarDetailViewModel(carListRepository, authRepository, firebaseAuth, savedStateHandle)

        viewModel.uiState.test {
            awaitItem()
            val finalState = awaitItem()
            assertFalse(finalState.isBuyer, "isBuyer should be false when the current user is the seller")
        }
    }

    @Test
    fun `loadCarDetails - error al cargar coche`() = runTest {
        val errorMessage = "Coche no encontrado"
        coEvery { carListRepository.getCarById(any(), any()) } returns Result.Error(Exception(errorMessage))
        every { mockCurrentUser.uid } returns "buyer456"

        viewModel = CarDetailViewModel(carListRepository, authRepository, firebaseAuth, savedStateHandle)

        viewModel.uiState.test {
            assertEquals(CarDataState.Loading, awaitItem().carDataState)

            val errorState = awaitItem().carDataState
            assertIs<CarDataState.Error>(errorState)
            assertEquals(errorMessage, errorState.message)
        }
    }

    @Test
    fun `onImagePageChanged - actualiza el indice de la imagen correctamente`() = runTest {
        coEvery { carListRepository.getCarById(any(), any()) } returns Result.Success(mockCar)
        coEvery { authRepository.getUserData(any()) } returns Result.Success(mockSeller)
        every { mockCurrentUser.uid } returns "buyer456"

        viewModel = CarDetailViewModel(carListRepository, authRepository, firebaseAuth, savedStateHandle)

        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            val newIndex = 2
            viewModel.onImagePageChanged(newIndex)

            assertEquals(newIndex, awaitItem().imagePagerIndex)
        }
    }
}