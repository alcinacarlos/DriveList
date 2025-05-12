package com.carlosalcina.drivelist.domain.model

data class Usuario(
    val email: String,
    val password: String,
    val nombre: String,
    val fotoUrl: String = ""
)