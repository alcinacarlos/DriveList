package com.carlosalcina.drivelist.domain.model

sealed class AuthError {
    data class NetworkError(val message: String?) : AuthError()
    data class UserNotFoundError(val message: String?) : AuthError()
    data class InvalidCredentials(val message: String?) : AuthError()
    data class UnknownError(val message: String?) : AuthError()
    data class EmailAlreadyInUse(val message: String?) : AuthError()
    data class WeakPassword(val message: String?) : AuthError()
}