package com.carlosalcina.drivelist.di

import com.carlosalcina.drivelist.data.datasource.CarRemoteDataSource
import com.carlosalcina.drivelist.data.datasource.FirestoreCarRemoteDataSource
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
        return FirebaseFirestore.getInstance()
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
}