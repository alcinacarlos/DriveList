package com.carlosalcina.drivelist.domain.error

sealed class FirestoreError {
    data class OperationFailed(val message: String?) : FirestoreError()
    data class NotFound(val message: String? = "Documento no encontrado.") : FirestoreError()
    data class PermissionDenied(val message: String? = "Permiso denegado.") : FirestoreError()
    data class Unknown(val message: String?) : FirestoreError()
}