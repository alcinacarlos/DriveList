package com.carlosalcina.drivelist.di

import com.carlosalcina.drivelist.data.repository.FirebaseAuthRepository
import com.carlosalcina.drivelist.data.repository.CredentialManagerGoogleSignInHandler
import com.carlosalcina.drivelist.domain.repository.AuthRepository
import com.carlosalcina.drivelist.domain.repository.GoogleSignInHandler
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return Firebase.auth
        }
    }

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGoogleSignInHandler(
        credentialManagerGoogleSignInHandler: CredentialManagerGoogleSignInHandler
    ): GoogleSignInHandler
}