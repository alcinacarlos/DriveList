package com.carlosalcina.drivelist.domain.repository

import com.carlosalcina.drivelist.domain.model.LocationAddress
import com.carlosalcina.drivelist.utils.Result

interface LocationRepository {
    /**
     * Obtiene la última localización conocida o la actual si es posible.
     * Requiere que los permisos ya hayan sido concedidos.
     */
    suspend fun getCurrentDeviceLocation(): Result<Pair<Double, Double>, Exception> // Latitud, Longitud

    /**
     * Convierte coordenadas (lat, lon) a detalles de dirección.
     */
    suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): Result<LocationAddress, Exception>

    /**
     * Obtiene detalles de dirección a partir de un código postal o nombre de ciudad.
     */
    suspend fun getAddressFromQuery(query: String): Result<LocationAddress, Exception>
}