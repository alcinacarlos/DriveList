package com.carlosalcina.drivelist

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.carlosalcina.drivelist.data.datasource.ImageStorageDataSource
import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.domain.repository.LocationRepository
import com.carlosalcina.drivelist.ui.viewmodel.UploadCarViewModel
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

@ExperimentalCoroutinesApi
class UploadCarViewModelTest {

    private lateinit var applicationContext: Context
    private lateinit var uploadRepository: CarUploadRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var imageStorageDataSource: ImageStorageDataSource
    private lateinit var locationRepository: LocationRepository
    private lateinit var viewModel: UploadCarViewModel

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var activeNetwork: Network
    private lateinit var networkCapabilities: NetworkCapabilities

    private val testDispatcher = StandardTestDispatcher()
    private val mockUser: FirebaseUser = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        applicationContext = mockk(relaxed = true)
        uploadRepository = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        imageStorageDataSource = mockk(relaxed = true)
        locationRepository = mockk(relaxed = true)

        connectivityManager = mockk(relaxed = true)
        activeNetwork = mockk(relaxed = true)
        networkCapabilities = mockk(relaxed = true)

        // 1. Cuando se pida el servicio de conectividad, devuelve nuestro mock.
        every { applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager

        // 2. Cuando se pregunte por la red activa, devuelve nuestra red mockeada.
        every { connectivityManager.activeNetwork } returns activeNetwork

        // 3. Cuando se pregunten las capacidades de esa red, devuelve nuestras capacidades mockeadas.
        every { connectivityManager.getNetworkCapabilities(activeNetwork) } returns networkCapabilities

        // 4. Cuando se pregunte si la red tiene capacidad de INTERNET, devuelve 'true'.
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true

        every { firebaseAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user123"

        coEvery { uploadRepository.getBrands() } returns Result.Success(listOf("Ford", "Seat"))

        viewModel = UploadCarViewModel(
            applicationContext, uploadRepository,
            firebaseAuth, imageStorageDataSource, locationRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init carga las marcas de coches correctamente`() = runTest {
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingBrands)
        assertEquals(listOf("Ford", "Seat"), state.brands)
    }

    @Test
    fun `onBrandSelected limpia campos dependientes y carga nuevos modelos`() = runTest {
        val newModels = listOf("Focus", "Fiesta")
        coEvery { uploadRepository.getModels("Ford") } returns Result.Success(newModels)

        viewModel.onBrandSelected("Ford")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingModels)
        assertEquals("Ford", state.selectedBrand)
        assertNull(state.selectedModel)
        assertEquals(newModels, state.models)
    }

}