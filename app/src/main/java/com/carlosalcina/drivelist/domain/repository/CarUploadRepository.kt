package com.carlosalcina.drivelist.domain.repository

import com.carlosalcina.drivelist.domain.model.CarForSale
import com.carlosalcina.drivelist.utils.Result

interface CarUploadRepository {
    /**
     * Sube un coche nuevo a Firestore.
     */
    suspend fun uploadCar(car: CarForSale): Result<Unit, Exception>

    /**
     * Actualiza un coche existente en Firestore.
     * Utilizará car.id para encontrar el documento a actualizar.
     */
    suspend fun updateCar(carToUpdate: CarForSale): Result<Unit, Exception>

    /**
     * Obtiene la lista de marcas de coches.
     */
    suspend fun getBrands(): Result<List<String>, Exception>

    /**
     * Obtiene los modelos para una marca específica.
     */
    suspend fun getModels(brandName: String): Result<List<String>, Exception>
    /**
     * Obtiene los tipos de cuerpo para una marca y modelo específicos.
     */
    suspend fun getBodyTypes(brandName: String, modelName: String): Result<List<String>, Exception>
    /**
     * Obtiene los tipos de combustible para una marca, modelo y tipo de cuerpo específicos.
     */
    suspend fun getFuelTypes(brandName: String, modelName: String, bodyTypeName: String): Result<List<String>, Exception>
    /**
     * Obtiene los años para una marca, modelo, tipo de cuerpo y tipo de combustible específicos.
     */
    suspend fun getYears(brandName: String, modelName: String, bodyTypeName: String, fuelTypeName: String): Result<List<String>, Exception>
    /**
     * Obtiene las versiones para una marca, modelo, tipo de cuerpo, tipo de combustible y año específicos.
     */
    suspend fun getVersions(brandName: String, modelName: String, bodyTypeName: String, fuelTypeName: String, yearName: String): Result<List<String>, Exception>

    suspend fun deleteCarFromFirestore(car: CarForSale): Result<Unit, Exception>
}