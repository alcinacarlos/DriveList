package com.carlosalcina.drivelist

import android.content.Context
import android.net.Uri
import com.carlosalcina.drivelist.data.datasource.ImageStorageDataSource
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.domain.repository.LocationRepository
import com.carlosalcina.drivelist.ui.viewmodel.EditCarViewModel
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
class EditCarViewModelTest {

    private lateinit var applicationContext: Context
    private lateinit var carRepository: CarListRepository
    private lateinit var uploadRepository: CarUploadRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var imageStorageDataSource: ImageStorageDataSource
    private lateinit var locationRepository: LocationRepository
    private lateinit var viewModel: EditCarViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val mockUser: FirebaseUser = mockk(relaxed = true)
    private val mockCar = CarForSale(
        id = "car123", userId = "user123", brand = "Ford", model = "Focus",
        bodyType = "Hatchback", fuelType = "Gasoline", year = "2020", version = "ST-Line",
        price = 20000.0, mileage = 15000, carColor = "ROJO"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        applicationContext = mockk(relaxed = true)
        carRepository = mockk(relaxed = true)
        uploadRepository = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        imageStorageDataSource = mockk(relaxed = true)
        locationRepository = mockk(relaxed = true)

        every { firebaseAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user123"

        viewModel = EditCarViewModel(
            applicationContext, carRepository, uploadRepository,
            firebaseAuth, imageStorageDataSource, locationRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCarDetails con exito carga todos los datos correctamente`() = runTest {
        coEvery { carRepository.getCarById(any(), any()) } returns Result.Success(mockCar)
        coEvery { uploadRepository.getBrands() } returns Result.Success(listOf("Ford", "Seat"))
        coEvery { uploadRepository.getModels(any()) } returns Result.Success(listOf("Focus", "Fiesta"))
        coEvery { uploadRepository.getBodyTypes(any(), any()) } returns Result.Success(listOf("Hatchback"))

        viewModel.loadCarDetails("car123")

        testScheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoadingCarDetails)
        assertFalse(finalState.isLoadingModels)
        assertFalse(finalState.isLoadingBodyTypes)
        assertEquals(mockCar.brand, finalState.selectedBrand)
        assertEquals(mockCar.price.toString(), finalState.price)
    }

    @Test
    fun `onBrandSelected limpia campos y carga nuevos modelos`() = runTest {
        val newModels = listOf("Ibiza", "Leon")
        coEvery { uploadRepository.getModels("Seat") } returns Result.Success(newModels)

        viewModel.onBrandSelected("Seat")

        testScheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoadingModels)
        assertEquals("Seat", finalState.selectedBrand)
        assertNull(finalState.selectedModel)
        assertEquals(newModels, finalState.models)
    }

    @Test
    fun `onImagesSelected anade nuevas imagenes sin exceder el limite`() {
        val uri1 = mockk<Uri>()
        val uri2 = mockk<Uri>()
        val initialUris = List(9) { mockk<Uri>() }
        viewModel.onImagesSelected(initialUris)

        viewModel.onImagesSelected(listOf(uri1, uri2))

        val finalState = viewModel.uiState.value
        assertEquals(10, finalState.newSelectedImageUris.size)
        assertTrue(finalState.newSelectedImageUris.contains(uri1))
        assertFalse(finalState.newSelectedImageUris.contains(uri2))
    }
}