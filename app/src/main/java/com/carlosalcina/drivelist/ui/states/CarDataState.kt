package com.carlosalcina.drivelist.ui.states

import com.carlosalcina.drivelist.domain.model.CarForSale

sealed class CarDataState {
    object Loading : CarDataState()
    data class Success(val car: CarForSale) : CarDataState()
    data class Error(val message: String) : CarDataState()
}