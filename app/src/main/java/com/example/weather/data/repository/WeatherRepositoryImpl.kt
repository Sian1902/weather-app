package com.example.weather.data.repository

import com.example.weather.data.local.WeatherCacheModel
import com.example.weather.data.local.WeatherLocalDataSource
import com.example.weather.data.remote.WeatherRemoteDataSource
import com.example.weather.data.remote.dto.CurrentWeatherDto
import com.example.weather.data.remote.dto.ForecastResponseDto
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class WeatherRepositoryImpl(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: WeatherLocalDataSource,
    private val gson: Gson = Gson()
) : WeatherRepository {

    override suspend fun getCurrentWeatherByCoordinates(
        lat: Double, lon: Double, units: String, lang: String
    ): CurrentWeatherDto = remoteDataSource.getCurrentWeatherByCoordinates(lat, lon, units, lang)

    override suspend fun getCurrentWeatherByCityName(
        cityName: String, units: String, lang: String
    ): CurrentWeatherDto = remoteDataSource.getCurrentWeatherByCityName(cityName, units, lang)

    override suspend fun getForecastByCoordinates(
        lat: Double, lon: Double, units: String, lang: String
    ): ForecastResponseDto = remoteDataSource.getForecastByCoordinates(lat, lon, units, lang)

    override suspend fun getForecastByCityName(
        cityName: String, units: String, lang: String
    ): ForecastResponseDto = remoteDataSource.getForecastByCityName(cityName, units, lang)

    override suspend fun getWeatherWithCache(
        cityName: String, units: String, lang: String
    ): WeatherResult = try {
        coroutineScope {
            val currentDeferred = async {
                remoteDataSource.getCurrentWeatherByCityName(cityName, units, lang)
            }
            val forecastDeferred = async {
                remoteDataSource.getForecastByCityName(cityName, units, lang)
            }
            val current = currentDeferred.await()
            val forecast = forecastDeferred.await()

            saveCache(cityName, current, forecast)

            WeatherResult.Live(current, forecast)
        }
    } catch (e: Exception) {
        loadCachedResult(cityName) ?: throw e
    }

    override suspend fun getWeatherWithCacheByCoords(
        lat: Double, lon: Double, units: String, lang: String
    ): WeatherResult {
        val cacheKey = "coords_${lat}_${lon}"
        return try {
            coroutineScope {
                val currentDeferred = async {
                    remoteDataSource.getCurrentWeatherByCoordinates(lat, lon, units, lang)
                }
                val forecastDeferred = async {
                    remoteDataSource.getForecastByCoordinates(lat, lon, units, lang)
                }
                val current = currentDeferred.await()
                val forecast = forecastDeferred.await()

                saveCache(current.name, current, forecast)

                saveCache(cacheKey, current, forecast)

                WeatherResult.Live(current, forecast)
            }
        } catch (e: Exception) {
            loadCachedResult(cacheKey) ?: throw e
        }
    }


    private suspend fun saveCache(
        key: String, current: CurrentWeatherDto, forecast: ForecastResponseDto
    ) {
        val model = WeatherCacheModel(
            current = current, forecast = forecast, cachedAtEpochMs = System.currentTimeMillis()
        )
        localDataSource.saveWeatherCache(key, gson.toJson(model))
    }

    private suspend fun loadCachedResult(key: String): WeatherResult.Cached? {
        val json = localDataSource.loadWeatherCache(key) ?: return null
        val model = runCatching {
            gson.fromJson(json, WeatherCacheModel::class.java)
        }.getOrNull() ?: return null

        return WeatherResult.Cached(
            current = model.current,
            forecast = model.forecast,
            cachedAtEpochMs = model.cachedAtEpochMs
        )
    }
}