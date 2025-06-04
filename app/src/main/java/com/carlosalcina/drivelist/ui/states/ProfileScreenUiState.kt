package com.carlosalcina.drivelist.ui.states

import com.carlosalcina.drivelist.domain.model.AuthUser
import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.domain.model.UserData

enum class EditableField {
    DISPLAY_NAME, PHOTO_URL, BIO
}

data class ProfileScreenUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val authUser: AuthUser? = null,
    val userData: UserData? = null,
    val userCars: List<CarForSale> = emptyList(),
    val isLoadingCars: Boolean = false,
    val carsErrorMessage: String? = null,

    // Estado para la edición de campos individuales
    val editingField: EditableField? = null, // Qué campo se está editando
    val fieldValueToEdit: String = "", // Valor actual del campo que se edita
    val isUpdatingField: Boolean = false,
    val fieldUpdateErrorMessage: String? = null,
    val fieldUpdateSuccessMessage: String? = null,

    // Estado para el diálogo de opciones de foto de perfil
    val showPhotoOptionsDialog: Boolean = false,

    // Estado para la subida de imagen de perfil
    val isUploadingPhoto: Boolean = false,
    val photoUploadErrorMessage: String? = null,

    val isAuthUser:Boolean = true
)
