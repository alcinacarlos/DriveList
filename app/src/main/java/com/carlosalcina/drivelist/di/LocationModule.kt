package com.carlosalcina.drivelist.di

import android.content.Context
import com.carlosalcina.drivelist.data.remote.GeoNamesApiService
import com.carlosalcina.drivelist.data.repository.LocationRepositoryImpl
import com.carlosalcina.drivelist.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(
        @ApplicationContext context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        geoNamesApiService: GeoNamesApiService
    ): LocationRepository {
        return LocationRepositoryImpl(context, fusedLocationClient, geoNamesApiService)
    }
}
