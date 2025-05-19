package com.carlosalcina.drivelist.di

import android.content.Context
import com.carlosalcina.drivelist.BuildConfig
import com.carlosalcina.drivelist.data.datasource.CarRemoteDataSource
import com.carlosalcina.drivelist.data.datasource.FirebaseImageStorageDataSource
import com.carlosalcina.drivelist.data.datasource.FirestoreCarRemoteDataSource
import com.carlosalcina.drivelist.data.datasource.ImageStorageDataSource
import com.carlosalcina.drivelist.data.remote.GeoNamesApiService
import com.carlosalcina.drivelist.data.repository.CarListRepositoryImpl
import com.carlosalcina.drivelist.data.repository.CarUploadRepositoryImpl
import com.carlosalcina.drivelist.data.repository.CredentialManagerGoogleSignInHandler
import com.carlosalcina.drivelist.data.repository.FirebaseAuthRepository
import com.carlosalcina.drivelist.data.repository.LocationRepositoryImpl
import com.carlosalcina.drivelist.data.repository.UserFavoriteRepositoryImpl
import com.carlosalcina.drivelist.domain.repository.AuthRepository
import com.carlosalcina.drivelist.domain.repository.CarListRepository
import com.carlosalcina.drivelist.domain.repository.CarUploadRepository
import com.carlosalcina.drivelist.domain.repository.GoogleSignInHandler
import com.carlosalcina.drivelist.domain.repository.LocationRepository
import com.carlosalcina.drivelist.domain.repository.UserFavoriteRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val GEONAMES_BASE_URL = "https://secure.geonames.org/"


    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GEONAMES_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGeoNamesApiService(retrofit: Retrofit): GeoNamesApiService {
        return retrofit.create(GeoNamesApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
    @Provides
    @Singleton
    fun provideLocationRepository(
        @ApplicationContext context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        geoAPIService: GeoNamesApiService
    ): LocationRepository {
        return LocationRepositoryImpl(context, fusedLocationClient, geoAPIService)
    }

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

    @Provides
    @Singleton
    fun provideCarListRepository(
        firestore: FirebaseFirestore,
        userFavoriteRepository: UserFavoriteRepository
    ): CarListRepository {
        return CarListRepositoryImpl(firestore, userFavoriteRepository)
    }

    // Repositorio para los favoritos del usuario
    @Provides
    @Singleton
    fun provideUserFavoriteRepository(
        firestore: FirebaseFirestore
    ): UserFavoriteRepository {
        return UserFavoriteRepositoryImpl(firestore)
    }
}