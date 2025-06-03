package com.carlosalcina.drivelist.ui.states

import android.net.Uri
import com.carlosalcina.drivelist.domain.model.CarColor
import com.carlosalcina.drivelist.domain.model.CarForSale

data class EditCarScreenState(
    val carIdToEdit: String? = null,
    val initialCarData: CarForSale? = null,
    val isLoadingInitialData: Boolean = true,
    val initialDataLoadError: String? = null,
    val canEditCar: Boolean = false,

    // Campos del formulario
    var brand: String? = null,
    var model: String? = null,
    var bodyType: String = "",
    var fuelType: String? = null,
    var year: String = "",
    var version: String = "",
    var selectedCarColor: CarColor? = null,
    var price: String = "",
    var mileage: String = "",
    var description: String = "",

    var selectedImageUris: List<Uri> = emptyList(), // URIs locales de nuevas imágenes
    var existingImageUrls: List<String> = emptyList(), // URLs de imágenes ya subidas
    var imagesToDelete: Set<String> = emptySet(), // URLs de imágenes a eliminar de Storage

    // Localización
    var finalComunidadAutonoma: String? = null,
    var finalCiudad: String? = null,
    var finalPostalCode: String? = null,
    var manualLocationInput: String = "",
    var isManualLocationValid: Boolean = true,
    var locationValidationMessage: String? = null,
    var isFetchingLocationDetails: Boolean = false,
    var locationGeneralErrorMessage: String? = null,

    // Listas para desplegables
    val availableBrands: List<String> = emptyList(),
    val availableModels: List<String> = emptyList(),
    val availableBodyTypes: List<String> = emptyList(),
    val availableFuelTypes: List<String> = emptyList(),
    val availableCarColors: List<CarColor> = emptyList(),
    val availableYears: List<String> = emptyList(),

    // Estados de operaciones
    val isLoadingBrands: Boolean = false,
    val brandLoadError: String? = null,
    val isLoadingModels: Boolean = false,
    val modelLoadError: String? = null,
    val isUploadingImages: Boolean = false,
    val imageProcessingMessage: String? = null,
    val isSavingCar: Boolean = false,
    val saveCarError: String? = null,
    val saveCarSuccess: Boolean = false
)
