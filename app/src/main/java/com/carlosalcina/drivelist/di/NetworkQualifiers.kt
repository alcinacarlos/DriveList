package com.carlosalcina.drivelist.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MeiliSearchRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeoNamesRetrofit