package com.carlosalcina.drivelist.data.datasource

import android.net.Uri
import com.carlosalcina.drivelist.utils.Result

interface ImageStorageDataSource {
    /**
     * Sube una imagen a Firebase Storage
     * @param localImageUri El URI local de la imagen a subir
     * @param storagePath La ruta completa en Firebase Storage donde se guardará la imagen (ej: "car_images/carId/imageName.jpg")
     * @return Un Result que contiene la URL de descarga de la imagen subida o una excepción
     */
    suspend fun uploadImage(localImageUri: Uri, storagePath: String): Result<String, Exception>

    /**
     * Elimina una imagen de Firebase Storage
     * @param storagePath Ruta completa en Firebase Storage del archivo a eliminar
     * @return Un Result que indica éxito o error
     */
    suspend fun deleteImage(storagePath: String): Result<Unit, Exception>
}
