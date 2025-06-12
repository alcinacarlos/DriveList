package com.carlosalcina.drivelist

import android.content.Context
import app.cash.turbine.test
import com.carlosalcina.drivelist.domain.repository.AuthRepository
import com.carlosalcina.drivelist.domain.repository.GoogleSignInHandler
import com.carlosalcina.drivelist.ui.viewmodel.LoginViewModel
import com.carlosalcina.drivelist.utils.Result
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
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
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var googleSignInHandler: GoogleSignInHandler
    private lateinit var viewModel: LoginViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val mockContext: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        googleSignInHandler = mockk(relaxed = true)
        viewModel = LoginViewModel(authRepository, googleSignInHandler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // Test de Logica Sincrona

    @Test
    fun `cuando email y password no estan vacios canLogin es true`() {
        viewModel.onEmailChanged("test@test.com")
        viewModel.onPasswordChanged("123456")

        val finalState = viewModel.uiState.value
        assertTrue(finalState.canLogin)
    }

    @Test
    fun `cuando email esta vacio canLogin es false`() {
        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("123456")

        val finalState = viewModel.uiState.value
        assertFalse(finalState.canLogin)
    }
    // Test de Login con Google

    @Test
    fun `iniciarSesionConGoogle con exito en ambos pasos actualiza a loginSuccess`() = runTest {
        val googleToken = "test_google_token"
        coEvery { googleSignInHandler.getGoogleIdToken(any(), any()) } returns Result.Success(googleToken)
        coEvery { authRepository.signInWithGoogleToken(googleToken) } returns Result.Success(mockk())

        viewModel.iniciarSesionConGoogle(mockContext)

        viewModel.uiState.test {
            skipItems(1)
            val successState = awaitItem()

            assertFalse(successState.isLoading)
            assertTrue(successState.loginSuccess)
            assertEquals("Inicio con Google exitoso", successState.generalMessage)
        }
    }

}