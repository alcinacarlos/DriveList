package com.carlosalcina.drivelist.ui.states

import android.net.Uri
import com.carlosalcina.drivelist.domain.model.CarColor

data class EditCarScreenState(
    val carId: String? = null,
    val isLoadingCarDetails: Boolean = true, // Empieza en true para mostrar carga inicial

    // Listas para los desplegables
    val brands: List<String> = emptyList(),
    val models: List<String> = emptyList(),
    val bodyTypes: List<String> = emptyList(),
    val fuelTypes: List<String> = emptyList(),
    val years: List<String> = emptyList(),
    val versions: List<String> = emptyList(),

    // Selecciones actuales
    val selectedBrand: String? = null,
    val selectedModel: String? = null,
    val selectedBodyType: String? = null,
    val selectedFuelType: String? = null,
    val selectedYear: String? = null,
    val selectedVersion: String? = null,

    // Campos de texto del formulario
    val price: String = "",
    val mileage: String = "",
    val description: String = "",

    // Colores
    val availableCarColors: List<CarColor> = CarColor.GAMA_COMPLETA(),
    val selectedCarColor: CarColor? = null,

    // Estado imágenes
    val existingImageUrls: List<String> = emptyList(), // URLs de Firebase
    val newSelectedImageUris: List<Uri> = emptyList(),  // URIs locales nuevas
    val isUploadingImages: Boolean = false,
    val imageUploadErrorMessage: String? = null,

    // Estados de carga para cada desplegable
    val isLoadingBrands: Boolean = false,
    val isLoadingModels: Boolean = false,
    val isLoadingBodyTypes: Boolean = false,
    val isLoadingFuelTypes: Boolean = false,
    val isLoadingYears: Boolean = false,
    val isLoadingVersions: Boolean = false,

    // Estado de la subida/actualización del coche
    val formUploadInProgress: Boolean = false,
    val formUploadSuccess: Boolean = false,
    val generalErrorMessage: String? = null,

    // Ubicacion
    val manualLocationInput: String = "",
    val isManualLocationValid: Boolean = true,
    val locationValidationMessage: String? = null,
    val finalComunidadAutonoma: String? = null,
    val finalCiudad: String? = null,
    val finalPostalCode: String? = null,
    val isRequestingLocationPermission: Boolean = false,
    val isFetchingLocationDetails: Boolean = false,
    val locationGeneralErrorMessage: String? = null
)