package com.carlosalcina.drivelist.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosalcina.drivelist.data.datasource.ImageStorageDataSource
import com.carlosalcina.drivelist.domain.model.CarSearchFilters
import com.carlosalcina.drivelist.domain.repository.AuthRepository
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.navigation.NavigationArgs
import com.carlosalcina.drivelist.ui.states.EditableField
import com.carlosalcina.drivelist.ui.states.ProfileScreenUiState
import com.carlosalcina.drivelist.utils.Result
import com.carlosalcina.drivelist.utils.Utils.capitalizeFirstLetter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val carListRepository: CarListRepository,
    private val imageStorageDataSource: ImageStorageDataSource,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileScreenUiState())
    val uiState: StateFlow<ProfileScreenUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val initialUserId: String? = savedStateHandle[NavigationArgs.PROFILE_USER_ID_ARG]
            if (initialUserId == null){
                _uiState.update { it.copy(isAuthUser = false) }
            }
            val currentAuthUser = authRepository.getCurrentFirebaseUser()
            if (currentAuthUser == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Usuario no autenticado.") }
                return@launch
            }
            _uiState.update { it.copy(authUser = currentAuthUser) }
            val userDataResult = if (_uiState.value.isAuthUser){
                authRepository.getCurrentUserData()
            }else{
                authRepository.getUserData(initialUserId!!)
            }

            when (userDataResult) {
                is Result.Success -> {
                    val fetchedUserData = userDataResult.data
                    _uiState.update {
                        it.copy(isLoading = false, userData = fetchedUserData)
                    }
                    fetchUserCars(fetchedUserData.uid)
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Fallo al cargar datos del usuario."
                        )
                    }
                }
            }
        }
    }

    private fun fetchUserCars(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCars = true, carsErrorMessage = null) }
            val filters = CarSearchFilters(userId = userId)
            when (val carsResult = carListRepository.searchCars(filters, limit = 50, currentUserId = userId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoadingCars = false, userCars = carsResult.data)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingCars = false,
                            carsErrorMessage = "Fallo al cargar los coches del usuario."
                        )
                    }
                }
            }
        }
    }

    // --- Edición de Foto de Perfil ---
    fun onProfilePhotoEditClicked() {
        _uiState.update { it.copy(showPhotoOptionsDialog = true) }
    }

    fun onDismissPhotoOptionsDialog() {
        _uiState.update { it.copy(showPhotoOptionsDialog = false) }
    }

    fun onDeleteProfilePhoto() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, photoUploadErrorMessage = null, showPhotoOptionsDialog = false) }
            val dataToUpdate = mapOf("photoURL" to "")
            when (authRepository.updateCurrentUserData(dataToUpdate)) {
                is Result.Success -> {
                    refreshUserData("Foto de perfil eliminada.")
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            photoUploadErrorMessage = "Fallo al eliminar la foto de perfil."
                        )
                    }
                }
            }
        }
    }

    fun onNewProfilePhotoSelected(imageUri: Uri?) {
        if (imageUri == null) return
        _uiState.update { it.copy(showPhotoOptionsDialog = false, isUploadingPhoto = true, photoUploadErrorMessage = null) }

        viewModelScope.launch {
            var downloadUrl = ""
            val storagePath = "users/${_uiState.value.userData!!.uid}/profile.jpg"
            val uploadResult = imageStorageDataSource.uploadImage(imageUri, storagePath)
             when (uploadResult) {
                 is Result.Success -> {
                     downloadUrl = uploadResult.data
                     val dataToUpdate = mapOf("photoURL" to downloadUrl)
                     when (authRepository.updateCurrentUserData(dataToUpdate)) {
                         is Result.Success -> refreshUserData("Foto de perfil actualizada.")
                         is Result.Error -> _uiState.update { it.copy(isUploadingPhoto = false, photoUploadErrorMessage = "Fallo al guardar nueva URL de foto.") }
                     }
                 }
                 is Result.Error -> {
                     _uiState.update { it.copy(isUploadingPhoto = false, photoUploadErrorMessage = "Fallo al subir la imagen.") }
                 }
             }
            _uiState.update { it.copy(isUploadingPhoto = false, photoUploadErrorMessage = "Funcionalidad de subida de imagen no implementada.") }
            val dataToUpdate = mapOf("photoURL" to downloadUrl)
            authRepository.updateCurrentUserData(dataToUpdate)
            refreshUserData()
        }
    }


    // --- Edición de Campos Individuales ---
    fun onEditField(field: EditableField) {
        val userData = _uiState.value.userData ?: return
        val currentValue = when (field) {
            EditableField.DISPLAY_NAME -> userData.displayName ?: _uiState.value.authUser?.displayName ?: ""
            EditableField.BIO -> userData.bio ?: ""
            EditableField.PHOTO_URL -> userData.photoURL ?: "" // Aunque PHOTO_URL se maneja diferente
        }
        _uiState.update {
            it.copy(
                editingField = field,
                fieldValueToEdit = currentValue,
                fieldUpdateErrorMessage = null,
                fieldUpdateSuccessMessage = null
            )
        }
    }

    fun onEditableFieldValueChanged(newValue: String) {
        _uiState.update { it.copy(fieldValueToEdit = newValue) }
    }

    fun onDismissEditFieldDialog() {
        _uiState.update { it.copy(editingField = null, fieldValueToEdit = "") }
    }

    fun saveFieldChange() {
        val fieldToEdit = _uiState.value.editingField ?: return
        val newValue = _uiState.value.fieldValueToEdit
        val originalValue = when (fieldToEdit) {
            EditableField.DISPLAY_NAME -> _uiState.value.userData?.displayName ?: _uiState.value.authUser?.displayName
            EditableField.BIO -> _uiState.value.userData?.bio
            EditableField.PHOTO_URL -> _uiState.value.userData?.photoURL
        }

        if (newValue == (originalValue ?: "")) { // Si no hay cambios, o si el original era null y el nuevo es ""
            _uiState.update { it.copy(editingField = null, fieldUpdateSuccessMessage = "No se hicieron cambios.") }
            return
        }


        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingField = true, fieldUpdateErrorMessage = null) }

            val fieldKey = when (fieldToEdit) {
                EditableField.DISPLAY_NAME -> "displayName"
                EditableField.BIO -> "bio"
                EditableField.PHOTO_URL -> "photoURL" // Este caso no debería ocurrir aquí normalmente
            }

            val dataToUpdate = mapOf(fieldKey to newValue.ifBlank { "" }) // Guardar null si está vacío

            when (authRepository.updateCurrentUserData(dataToUpdate)) {
                is Result.Success -> {
                    refreshUserData("${fieldKey.capitalizeFirstLetter()} actualizado.")
                    _uiState.update { it.copy(editingField = null) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingField = false,
                            fieldUpdateErrorMessage = "Fallo al actualizar ${fieldKey}."
                        )
                    }
                }
            }
        }
    }

    private fun refreshUserData(successMessage: String? = null) {
        viewModelScope.launch {
            when (val userDataResult = authRepository.getCurrentUserData()) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        userData = userDataResult.data,
                        isUpdatingField = false,
                        isUploadingPhoto = false,
                        fieldUpdateSuccessMessage = successMessage,
                        photoUploadErrorMessage = null // Clear photo error on general refresh
                    )
                }
                is Result.Error -> _uiState.update {
                    it.copy( // Still success for update, but failed to refresh immediately
                        isUpdatingField = false,
                        isUploadingPhoto = false,
                        fieldUpdateErrorMessage = "Datos actualizados, pero falló la actualización inmediata de la UI.",
                    )
                }
            }
        }
    }

    fun clearUpdateMessages() {
        _uiState.update {
            it.copy(
                fieldUpdateSuccessMessage = null,
                fieldUpdateErrorMessage = null,
                photoUploadErrorMessage = null
            )
        }
    }
}