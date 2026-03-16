package com.example.weather.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

interface LocationProvider {
    suspend fun getLastLocation(): Location?

    suspend fun getCurrentLocation(): Location?
}

class LocationProviderImpl(context: Context) : LocationProvider {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Location? = try {
        fusedClient.lastLocation.await()
    } catch (_: Exception) {
        null
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? {
        val cts = CancellationTokenSource()
        return try {
            withTimeoutOrNull(10_000L) {
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token
                ).await()
            }
        } catch (_: Exception) {
            null
        } finally {
            cts.cancel()
        }
    }
}