package com.carlosalcina.drivelist.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.carlosalcina.drivelist.BuildConfig
import com.carlosalcina.drivelist.data.remote.GeoNamesApiService
import com.carlosalcina.drivelist.domain.model.LocationAddress
import com.carlosalcina.drivelist.domain.repository.LocationRepository
import com.carlosalcina.drivelist.utils.Result
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val geoNamesApiService: GeoNamesApiService
) : LocationRepository {

    private val geocoder by lazy { Geocoder(context, Locale("es", "ES")) }

    override suspend fun getCurrentDeviceLocation(): Result<Pair<Double, Double>, Exception> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Result.Error(SecurityException("Location permission not granted."))
        }
        return try {
            // Usar getCurrentLocation para una localización más fresca
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, // O PRIORITY_HIGH_ACCURACY
                CancellationTokenSource().token // Para permitir cancelación
            ).await() // Usa await() de kotlinx-coroutines-play-services

            if (location != null) {
                Result.Success(Pair(location.latitude, location.longitude))
            } else {
                Result.Error(Exception("Failed to get current location."))
            }
        } catch (e: SecurityException) {
            Log.e("LocationRepo", "Permission error getting location", e)
            Result.Error(e) // Permiso denegado o no comprobado correctamente antes
        } catch (e: Exception) {
            Log.e("LocationRepo", "Error getting current location", e)
            Result.Error(e)
        }
    }


    override suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): Result<LocationAddress, Exception> {
        return withContext(Dispatchers.IO) { // Geocoder puede ser bloqueante
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    var addressResult: LocationAddress? = null
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses -> // Callback para API 33+
                        val address = addresses.firstOrNull()
                        addressResult = if (address != null) {
                            LocationAddress(
                                ciudad = address.locality,
                                comunidadAutonoma = address.adminArea,
                                postalCode = address.postalCode,
                                countryCode = address.countryCode
                            )
                        } else {
                            null
                        }
                    }
                    repeat(3) {
                        if (addressResult != null) return@withContext Result.Success(addressResult!!)
                        kotlinx.coroutines.delay(200)
                    }
                    if (addressResult != null) Result.Success(addressResult!!)
                    else Result.Error(Exception("Geocoder (API 33+) did not return an address or timed out."))

                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val address = addresses?.firstOrNull()
                    if (address != null) {
                        Result.Success(
                            LocationAddress(
                                ciudad = address.locality,
                                comunidadAutonoma = address.adminArea,
                                postalCode = address.postalCode,
                                countryCode = address.countryCode
                            )
                        )
                    } else {
                        Result.Error(Exception("Geocoder returned no address."))
                    }
                }
            } catch (e: Exception) {
                Log.e("LocationRepo", "Error in getAddressFromCoordinates", e)
                Result.Error(e)
            }
        }
    }


    override suspend fun getAddressFromQuery(query: String): Result<LocationAddress, Exception> {

        val geonamesUsername = BuildConfig.GEONAMES_USERNAME

        if (geonamesUsername.isBlank()) {
            Log.e("LocationRepo", "GeoNames username not configured.")
            return Result.Error(Exception("GeoNames username not configured."))
        }

        return try {
            val response = geoNamesApiService.searchPostalCode(
                postalCode = query,
                username = geonamesUsername
            )

            if (response.isSuccessful) {
                val geoNamesData = response.body()
                val postalCodeInfo = geoNamesData?.postalCodes?.firstOrNull()

                if (postalCodeInfo != null) {
                    Result.Success(
                        LocationAddress(
                            ciudad = postalCodeInfo.placeName ?: postalCodeInfo.adminName2,
                            comunidadAutonoma = postalCodeInfo.adminName1,
                            postalCode = postalCodeInfo.postalCode,
                            countryCode = "ES" // Ya lo sabemos
                        )
                    )
                } else {
                    Log.d("LocationRepo", "GeoNames returned no postal code info for query: $query, response: ${geoNamesData?.postalCodes}")
                    Result.Error(Exception("No location found for postal code: $query. List was empty or null.")) // Lista vacía
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("LocationRepo", "GeoNames API error: ${response.code()} - $errorBody")
                Result.Error(Exception("GeoNames API error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("LocationRepo", "Exception during GeoNames API call for query '$query'", e)
            Result.Error(e)
        }
    }
}