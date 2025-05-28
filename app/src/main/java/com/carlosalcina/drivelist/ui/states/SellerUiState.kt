package com.carlosalcina.drivelist.ui.states

import com.carlosalcina.drivelist.domain.model.UserData

sealed class SellerUiState {
    object Loading : SellerUiState()
    data class Success(val userData: UserData) : SellerUiState()
    data class Error(val message: String) : SellerUiState()
}