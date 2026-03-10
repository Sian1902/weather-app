package com.example.weather.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.weatherDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "weather_cache")

interface WeatherLocalDataSource {
    suspend fun saveWeatherCache(cityName: String, json: String)
    suspend fun loadWeatherCache(cityName: String): String?
    suspend fun clearCache(cityName: String)
}

class WeatherLocalDataSourceImpl(
    private val context: Context
) : WeatherLocalDataSource {

    private fun cacheKey(cityName: String) =
        stringPreferencesKey("weather_cache_${cityName.lowercase().trim()}")

    override suspend fun saveWeatherCache(cityName: String, json: String) {
        context.weatherDataStore.edit { prefs ->
            prefs[cacheKey(cityName)] = json
        }
    }

    override suspend fun loadWeatherCache(cityName: String): String? =
        context.weatherDataStore.data
            .map { prefs -> prefs[cacheKey(cityName)] }
            .firstOrNull()

    override suspend fun clearCache(cityName: String) {
        context.weatherDataStore.edit { prefs ->
            prefs.remove(cacheKey(cityName))
        }
    }
}