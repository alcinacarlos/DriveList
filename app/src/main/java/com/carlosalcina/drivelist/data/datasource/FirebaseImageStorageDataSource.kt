package com.carlosalcina.drivelist.data.datasource

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class FirebaseImageStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : ImageStorageDataSource {

    override suspend fun uploadImage(localImageUri: Uri, storagePath: String): Result<String, Exception> {
        return try {
            val inputStream = context.contentResolver.openInputStream(localImageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            val outputStream = ByteArrayOutputStream()
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream) // Calidad 75%
            val compressedData = outputStream.toByteArray()

            val storageRef = storage.reference.child(storagePath)
            val uploadTask = storageRef.putBytes(compressedData).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            Result.Success(downloadUrl)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
