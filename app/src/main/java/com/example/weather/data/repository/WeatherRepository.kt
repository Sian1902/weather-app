package com.example.weather.data.repository

import com.example.weather.data.remote.CurrentWeatherDto
import com.example.weather.data.remote.ForecastResponseDto

interface WeatherRepository {

    suspend fun getCurrentWeatherByCoordinates(
        lat: Double,
        lon: Double,
        units: String = "metric",
        lang: String = "en"
    ): CurrentWeatherDto

    suspend fun getCurrentWeatherByCityName(
        cityName: String,
        units: String = "metric",
        lang: String = "en"
    ): CurrentWeatherDto

    suspend fun getForecastByCoordinates(
        lat: Double,
        lon: Double,
        units: String = "metric",
        lang: String = "en"
    ): ForecastResponseDto

    suspend fun getForecastByCityName(
        cityName: String,
        units: String = "metric",
        lang: String = "en"
    ): ForecastResponseDto

    suspend fun getWeatherWithCache(
        cityName: String,
        units: String = "metric",
        lang: String = "en"
    ): WeatherResult

    suspend fun getWeatherWithCacheByCoords(
        lat: Double,
        lon: Double,
        units: String = "metric",
        lang: String = "en"
    ): WeatherResult
}

sealed class WeatherResult {
    data class Live(
        val current: CurrentWeatherDto,
        val forecast: ForecastResponseDto
    ) : WeatherResult()

    data class Cached(
        val current: CurrentWeatherDto,
        val forecast: ForecastResponseDto,
        val cachedAtEpochMs: Long
    ) : WeatherResult()
}