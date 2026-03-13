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
    /** Returns the last cached location instantly. Null if none available. */
    suspend fun getLastLocation(): Location?

    /** Requests a fresh fix. Returns null if GPS is off or no fix within 10 s. */
    suspend fun getCurrentLocation(): Location?
}

class LocationProviderImpl(context: Context) : LocationProvider {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Location? =
        try { fusedClient.lastLocation.await() } catch (_: Exception) { null }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? {
        val cts = CancellationTokenSource()
        return try {
            // withTimeoutOrNull cancels the Task and returns null if no fix arrives
            // within 10 seconds — prevents hanging on emulators with no mock location.
            withTimeoutOrNull(10_000L) {
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cts.token
                ).await()
            }
        } catch (_: Exception) {
            null
        } finally {
            cts.cancel()
        }
    }
}