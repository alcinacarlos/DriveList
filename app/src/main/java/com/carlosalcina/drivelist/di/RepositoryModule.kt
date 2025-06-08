package com.carlosalcina.drivelist.di

import android.content.Context
import com.carlosalcina.drivelist.data.datasource.CarRemoteDataSource
import com.carlosalcina.drivelist.data.datasource.FirebaseImageStorageDataSource
import com.carlosalcina.drivelist.data.datasource.FirestoreCarRemoteDataSource
import com.carlosalcina.drivelist.data.datasource.ImageStorageDataSource
import com.carlosalcina.drivelist.data.repository.CarListRepositoryImpl
import com.carlosalcina.drivelist.data.repository.CarUploadRepositoryImpl
import com.carlosalcina.drivelist.data.repository.ChatRepositoryImpl
import com.carlosalcina.drivelist.data.repository.UserFavoriteRepositoryImpl
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.domain.repository.ChatRepository
import com.carlosalcina.drivelist.domain.repository.UserFavoriteRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

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
    fun provideImageStorageDataSource(
        storage: FirebaseStorage,
        @ApplicationContext context: Context
    ): ImageStorageDataSource {
        return FirebaseImageStorageDataSource(storage, context)
    }

    @Provides
    @Singleton
    fun provideUserFavoriteRepository(
        firestore: FirebaseFirestore
    ): UserFavoriteRepository {
        return UserFavoriteRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideCarListRepository(
        firestore: FirebaseFirestore,
        userFavoriteRepository: UserFavoriteRepository
    ): CarListRepository {
        return CarListRepositoryImpl(firestore, userFavoriteRepository)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore
    ): ChatRepository {
        return ChatRepositoryImpl(firestore)
    }
}
