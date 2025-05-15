package com.carlosalcina.drivelist.data.datasource

import android.net.Uri
import com.carlosalcina.drivelist.utils.Result
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseImageStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage
) : ImageStorageDataSource {

    override suspend fun uploadImage(localImageUri: Uri, storagePath: String): Result<String, Exception> {
        return try {
            val storageRef = storage.reference.child(storagePath)
            val uploadTask = storageRef.putFile(localImageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            Result.Success(downloadUrl)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}