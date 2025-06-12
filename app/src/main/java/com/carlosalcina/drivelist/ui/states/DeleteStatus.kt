package com.carlosalcina.drivelist.ui.states

sealed class DeleteStatus {
    object Idle : DeleteStatus()
    object Loading : DeleteStatus()
    data class Error(val message: String) : DeleteStatus()
    object Success : DeleteStatus()
}