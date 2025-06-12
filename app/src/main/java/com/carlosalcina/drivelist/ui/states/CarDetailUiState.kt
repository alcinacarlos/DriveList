package com.carlosalcina.drivelist.ui.states

data class CarDetailUiState(
    val carDataState: CarDataState = CarDataState.Loading,
    val sellerUiState: SellerUiState = SellerUiState.Loading,
    val imagePagerIndex: Int = 0,
    var isBuyer: Boolean = true,
    var currentUserId: String = "",
    val deleteStatus: DeleteStatus = DeleteStatus.Idle
)