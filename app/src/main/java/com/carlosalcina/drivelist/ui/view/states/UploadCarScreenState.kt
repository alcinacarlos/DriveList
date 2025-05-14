package com.carlosalcina.drivelist.ui.view.states

import android.net.Uri

data class UploadCarScreenState(
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
    val imageUris: List<Uri> = emptyList(),

    // Estados de carga para cada desplegable
    val isLoadingBrands: Boolean = false,
    val isLoadingModels: Boolean = false,
    val isLoadingBodyTypes: Boolean = false,
    val isLoadingFuelTypes: Boolean = false,
    val isLoadingYears: Boolean = false,
    val isLoadingVersions: Boolean = false,

    // Estado de la subida del coche
    val uploadInProgress: Boolean = false,
    val uploadSuccess: Boolean = false,
    val generalErrorMessage: String? = null // Para errores de carga o de subida
)