package com.carlosalcina.drivelist.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.domain.error.FirestoreError
import com.carlosalcina.drivelist.domain.repository.AuthRepository
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.ui.navigation.NavigationArgs
import com.carlosalcina.drivelist.ui.states.CarDataState
import com.carlosalcina.drivelist.ui.states.CarDetailUiState
import com.carlosalcina.drivelist.ui.states.SellerUiState
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarDetailViewModel @Inject constructor(
    private val carListRepository: CarListRepository,
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: String = savedStateHandle.get<String>(NavigationArgs.CAR_ID_ARG) ?: ""

    private val _uiState = MutableStateFlow(CarDetailUiState())
    val uiState: StateFlow<CarDetailUiState> = _uiState.asStateFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CarDetailUiState()
    )

    init {
        if (carId.isNotBlank()) {
            loadCarDetails()
        } else {
            _uiState.update {
                it.copy(carDataState = CarDataState.Error("ID de coche no válido."))
            }
        }
    }

    private fun loadCarDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(carDataState = CarDataState.Loading) }
            val currentUserId = firebaseAuth.currentUser?.uid
            _uiState.update { it.copy(currentUserId = currentUserId ?: "") }
            when (val result = carListRepository.getCarById(carId, currentUserId)) {
                is Result.Success -> {
                    val car = result.data
                    _uiState.update {
                        it.copy(carDataState = CarDataState.Success(car))
                    }
                    if (car.userId == firebaseAuth.currentUser?.uid) {
                        _uiState.value.isBuyer = false
                    }

                    loadSellerInfo(car.userId)
                }

                is Result.Error -> {
                    val errorMessage = handleCarLoadError(result.error)
                    _uiState.update {
                        it.copy(carDataState = CarDataState.Error(errorMessage))
                    }
                }
            }
        }
    }

    private fun loadSellerInfo(sellerId: String) {
        if (sellerId.isBlank()) {
            _uiState.update {
                it.copy(sellerUiState = SellerUiState.Error("ID de vendedor no disponible."))
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(sellerUiState = SellerUiState.Loading) }
            when (val result = authRepository.getUserData(sellerId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(sellerUiState = SellerUiState.Success(result.data))
                    }
                }

                is Result.Error -> {
                    val errorMessage = handleFirestoreError(result.error)
                    _uiState.update {
                        it.copy(sellerUiState = SellerUiState.Error(errorMessage))
                    }
                }
            }
        }
    }

    fun onImagePageChanged(index: Int) {
        _uiState.update { it.copy(imagePagerIndex = index) }
    }

    private fun handleCarLoadError(exception: Exception): String {
        return exception.message ?: "Error al cargar los detalles del coche."
    }

    private fun handleFirestoreError(error: FirestoreError): String {
        return when (error) {
            is FirestoreError.NotFound -> error.message ?: "Información del vendedor no encontrada."
            is FirestoreError.PermissionDenied -> error.message
                ?: "No tienes permiso para ver esta información."

            is FirestoreError.OperationFailed -> error.message
                ?: "Falló la operación al obtener datos del vendedor."

            is FirestoreError.Unknown -> error.message
                ?: "Error desconocido al obtener datos del vendedor."
        }
    }

    fun retryLoadCarDetails() {
        if (carId.isNotBlank()) {
            loadCarDetails()
        }
    }
}