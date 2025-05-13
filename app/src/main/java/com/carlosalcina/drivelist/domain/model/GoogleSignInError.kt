package com.carlosalcina.drivelist.domain.model

sealed class GoogleSignInError {
    data class ApiError(val message: String?) : GoogleSignInError()
    object UserCancelled : GoogleSignInError()
    data class NoCredentialFound(val message: String?) : GoogleSignInError()
    data class UnexpectedCredentialType(val message: String?) : GoogleSignInError()
    data class UnknownError(val message: String?) : GoogleSignInError()
}