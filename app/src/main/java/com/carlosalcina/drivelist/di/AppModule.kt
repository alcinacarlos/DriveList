package com.carlosalcina.drivelist.di

import com.carlosalcina.drivelist.data.datasource.CarRemoteDataSource
import com.carlosalcina.drivelist.data.datasource.FirebaseImageStorageDataSource
import com.carlosalcina.drivelist.data.datasource.FirestoreCarRemoteDataSource
import com.carlosalcina.drivelist.data.datasource.ImageStorageDataSource
import com.carlosalcina.drivelist.data.repository.CarUploadRepositoryImpl
import com.carlosalcina.drivelist.data.repository.FirebaseAuthRepository
import com.carlosalcina.drivelist.data.repository.CredentialManagerGoogleSignInHandler
import com.carlosalcina.drivelist.domain.repository.AuthRepository
import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.domain.repository.GoogleSignInHandler
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository {
        return FirebaseAuthRepository(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideGoogleSignInHandler(): GoogleSignInHandler {
        return CredentialManagerGoogleSignInHandler()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        // Configuración de la caché de Firestore
        val newCacheSizeBytes = 500 * 1024 * 1024L //500 MB
        // El tamaño mínimo de la caché es 1 MB (1 * 1024 * 1024L)
        // El tamaño por defecto es 100 MB.
        val settings = FirebaseFirestoreSettings.Builder(firestore.firestoreSettings)
            .setLocalCacheSettings(
                PersistentCacheSettings.newBuilder()
                    .setSizeBytes(newCacheSizeBytes)
                    .build()
            )
            .build()

        firestore.firestoreSettings = settings

        return firestore
    }


    @Provides
    @Singleton
    fun provideCarRemoteDataSource(
        firestore: FirebaseFirestore
    ): CarRemoteDataSource {
        return FirestoreCarRemoteDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideCarUploadRepository(
        remoteDataSource: CarRemoteDataSource
    ): CarUploadRepository {
        return CarUploadRepositoryImpl(remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideImageStorageDataSource(storage: FirebaseStorage): ImageStorageDataSource {
        return FirebaseImageStorageDataSource(storage)
    }
}