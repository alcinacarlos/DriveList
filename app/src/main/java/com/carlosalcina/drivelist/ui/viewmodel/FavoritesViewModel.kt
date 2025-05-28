package com.carlosalcina.drivelist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.domain.repository.UserFavoriteRepository
import com.carlosalcina.drivelist.domain.usecase.ToggleFavoriteCarUseCase
import com.carlosalcina.drivelist.ui.states.FavoritesUiState
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val userFavoriteRepository: UserFavoriteRepository,
    private val carListRepository: CarListRepository,
    private val toggleFavoriteCarUseCase: ToggleFavoriteCarUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FavoritesUiState()
    )

    init {
        observeAuthStateAndLoadFavorites()
    }

    private fun observeAuthStateAndLoadFavorites() {
        viewModelScope.launch {
            firebaseAuth.addAuthStateListener { auth ->
                val user = auth.currentUser
                if (user != null) {
                    _uiState.update { it.copy(isAuthenticated = true) }
                    loadFavoriteCarIds(user.uid)
                } else {
                    _uiState.update {
                        it.copy(
                            isAuthenticated = false,
                            favoriteCars = emptyList(),
                            isLoading = false,
                            error = "Debes iniciar sesión para ver tus favoritos."
                        )
                    }
                }
            }
        }
        firebaseAuth.currentUser?.uid?.let { userId ->
            _uiState.update { it.copy(isAuthenticated = true) }
            loadFavoriteCarIds(userId)
        } ?: _uiState.update {
            it.copy(
                isAuthenticated = false, error = "Debes iniciar sesión para ver tus favoritos."
            )
        }
    }

    private fun loadFavoriteCarIds(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = userFavoriteRepository.getUserFavoriteCarIds(userId)) {
                is Result.Success -> {
                    val favoriteIds = result.data
                    if (favoriteIds.isEmpty()) {
                        _uiState.update {
                            it.copy(favoriteCars = emptyList(), isLoading = false)
                        }
                    } else {
                        loadFavoriteCarsDetails(favoriteIds, userId)
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al obtener IDs de favoritos: ${result.error.message}"
                        )
                    }
                }
            }
        }
    }

    private fun loadFavoriteCarsDetails(favoriteIds: List<String>, currentUserId: String) {
        viewModelScope.launch {
            val deferredCarDetails = favoriteIds.map { carId ->
                async { carListRepository.getCarById(carId, currentUserId) }
            }

            val results = deferredCarDetails.awaitAll()
            val successfullyFetchedCars = mutableListOf<CarForSale>()
            var anErrorOccurred = false

            results.forEach { result ->
                when (result) {
                    is Result.Success -> {
                        successfullyFetchedCars.add(result.data.copy(isFavoriteByCurrentUser = true))
                    }

                    is Result.Error -> {
                        anErrorOccurred = true
                    }
                }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    favoriteCars = successfullyFetchedCars,
                    error = if (anErrorOccurred && successfullyFetchedCars.isEmpty()) "Error al cargar detalles de todos los favoritos."
                    else if (anErrorOccurred) "Error al cargar detalles de algunos favoritos."
                    else null
                )
            }
        }
    }

    fun toggleFavoriteStatus(carId: String) {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            _uiState.update { it.copy(error = "Debes iniciar sesión.") }
            return
        }

        val carInList = _uiState.value.favoriteCars.find { it.id == carId }
        val isCurrentlyFavorite = carInList?.isFavoriteByCurrentUser != false

        _uiState.update { currentState ->
            currentState.copy(
                isTogglingFavorite = currentState.isTogglingFavorite + (carId to true), error = null
            )
        }

        viewModelScope.launch {
            when (val result = toggleFavoriteCarUseCase(
                userId = currentUserId, carId = carId, isCurrentlyFavorite = isCurrentlyFavorite
            )) {
                is Result.Success -> {
                    val updatedCars = _uiState.value.favoriteCars.filterNot { it.id == carId }
                    _uiState.update { currentState ->
                        currentState.copy(
                            isTogglingFavorite = currentState.isTogglingFavorite - carId,
                            favoriteCars = updatedCars
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isTogglingFavorite = currentState.isTogglingFavorite - carId,
                            error = result.error.message ?: "Error al actualizar favorito."
                        )
                    }
                }
            }
        }
    }

    fun refreshFavorites() {
        firebaseAuth.currentUser?.uid?.let { userId ->
            loadFavoriteCarIds(userId)
        } ?: _uiState.update {
            it.copy(
                isAuthenticated = false,
                error = "Debes iniciar sesión para ver tus favoritos.",
                isLoading = false,
                favoriteCars = emptyList()
            )
        }
    }
}