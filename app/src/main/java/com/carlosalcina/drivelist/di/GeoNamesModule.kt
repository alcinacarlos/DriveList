package com.carlosalcina.drivelist.di

import com.carlosalcina.drivelist.data.remote.GeoNamesApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeoNamesModule {

    private const val BASE_URL = "http://api.geonames.org/"

    @Provides
    @Singleton
    fun provideGeoNamesRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGeoNamesApiService(retrofit: Retrofit): GeoNamesApiService {
        return retrofit.create(GeoNamesApiService::class.java)
    }
}
