package com.carlosalcina.drivelist.di

import android.content.Context
import com.carlosalcina.drivelist.BuildConfig
import com.carlosalcina.drivelist.data.datasource.CarRemoteDataSource
import com.carlosalcina.drivelist.data.datasource.FirebaseImageStorageDataSource
import com.carlosalcina.drivelist.data.datasource.FirestoreCarRemoteDataSource
import com.carlosalcina.drivelist.data.datasource.ImageStorageDataSource
import com.carlosalcina.drivelist.data.remote.GeoNamesApiService
import com.carlosalcina.drivelist.data.remote.MeiliSearchApi
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
import com.carlosalcina.drivelist.utils.FirebaseUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Base URLs
    private const val GEONAMES_BASE_URL = "https://secure.geonames.org/"
    private const val MEILI_SEARCH_BASE_URL = "https://meilisearch.alcina.es/"

    // API Keys
    private const val MEILI_API_KEY = "Pestillo123Pestillo123"

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
    }

    // --- PRUEBA: Un ÚNICO OkHttpClient COMPARTIDO ---
    @Provides
    @Singleton
    fun provideSharedOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        // Usaremos la configuración de MeiliSearch aquí, ya que incluye el interceptor de API Key.
        // GeoNames simplemente ignorará el encabezado de autorización si no lo necesita.
        // El objetivo es ver si el SSL Handshake funciona para AMBAS APIs con un solo cliente.
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain -> // Interceptor para la API Key de MeiliSearch
                val originalRequest = chain.request()
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $MEILI_API_KEY")
                    .build()
                chain.proceed(newRequest)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Comenta o elimina tus OkHttpClient específicos anteriores
    // @Provides @Singleton @GeoNamesOkHttpClient fun provideGeoNamesOkHttpClient(...) { ... }
    // @Provides @Singleton @MeiliSearchOkHttpClient fun provideMeiliSearchOkHttpClient(...) { ... }


    // --- Proveedores de Retrofit usando el OkHttpClient COMPARTIDO ---
    @Provides
    @Singleton
    @GeoNamesRetrofit
    fun provideGeoNamesRetrofit(
        okHttpClient: OkHttpClient, // Hilt inyectará el provideSharedOkHttpClient
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GEONAMES_BASE_URL)
            .client(okHttpClient) // Usa el cliente compartido
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @MeiliSearchRetrofit
    fun provideMeiliSearchRetrofit(
        okHttpClient: OkHttpClient, // Hilt inyectará el provideSharedOkHttpClient
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(MEILI_SEARCH_BASE_URL)
            .client(okHttpClient) // Usa el cliente compartido
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Los proveedores de servicios API no cambian
    @Provides
    @Singleton
    fun provideGeoNamesApiService(@GeoNamesRetrofit retrofit: Retrofit): GeoNamesApiService {
        return retrofit.create(GeoNamesApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMeiliSearchApi(@MeiliSearchRetrofit retrofit: Retrofit): MeiliSearchApi {
        return retrofit.create(MeiliSearchApi::class.java)
    }

    // --- Tus otros providers existentes ---
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
        geoAPIService: GeoNamesApiService // Hilt inyectará la instancia correcta aquí
    ): LocationRepository {
        return LocationRepositoryImpl(context, fusedLocationClient, geoAPIService)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseUtils.getInstance() // Considera inyectar FirebaseAuth.getInstance() directamente si es posible
    }

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth, fireStore: FirebaseFirestore): AuthRepository {
        return FirebaseAuthRepository(firebaseAuth, fireStore)
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
        val newCacheSizeBytes = 500 * 1024 * 1024L
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
        remoteDataSource: CarRemoteDataSource,
        meiliSearchApi: MeiliSearchApi
    ): CarUploadRepository {
        // Ajusta el constructor si es necesario
        return CarUploadRepositoryImpl(remoteDataSource, meiliSearchApi)
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
        userFavoriteRepository: UserFavoriteRepository,
        meiliSearchApi: MeiliSearchApi // ✨ Inyecta MeiliSearchApi aquí
    ): CarListRepository {
        // Asumo que CarListRepositoryImpl usaba MeiliSeachClient.instance
        // Ahora lo recibirá por inyección de dependencias.
        return CarListRepositoryImpl(firestore, userFavoriteRepository, meiliSearchApi)
    }

    @Provides
    @Singleton
    fun provideUserFavoriteRepository(
        firestore: FirebaseFirestore
    ): UserFavoriteRepository {
        return UserFavoriteRepositoryImpl(firestore)
    }
}