package com.example.weather.data.remote

import com.example.weather.data.remote.api.WeatherApiService
import com.example.weather.data.remote.dto.CurrentWeatherDto
import com.example.weather.data.remote.dto.ForecastResponseDto

interface WeatherRemoteDataSource {

    suspend fun getCurrentWeatherByCoordinates(
        lat: Double, lon: Double, units: String, lang: String
    ): CurrentWeatherDto

    suspend fun getCurrentWeatherByCityName(
        cityName: String, units: String, lang: String
    ): CurrentWeatherDto

    suspend fun getForecastByCoordinates(
        lat: Double, lon: Double, units: String, lang: String
    ): ForecastResponseDto

    suspend fun getForecastByCityName(
        cityName: String, units: String, lang: String
    ): ForecastResponseDto
}

class WeatherRemoteDataSourceImpl(
    private val apiService: WeatherApiService, private val apiKey: String
) : WeatherRemoteDataSource {

    override suspend fun getCurrentWeatherByCoordinates(
        lat: Double, lon: Double, units: String, lang: String
    ): CurrentWeatherDto = apiService.getCurrentWeatherByCoordinates(
        lat = lat, lon = lon, apiKey = apiKey, units = units, lang = lang
    )

    override suspend fun getCurrentWeatherByCityName(
        cityName: String, units: String, lang: String
    ): CurrentWeatherDto = apiService.getCurrentWeatherByCityName(
        cityName = cityName, apiKey = apiKey, units = units, lang = lang
    )

    override suspend fun getForecastByCoordinates(
        lat: Double, lon: Double, units: String, lang: String
    ): ForecastResponseDto = apiService.getForecastByCoordinates(
        lat = lat, lon = lon, apiKey = apiKey, units = units, lang = lang
    )

    override suspend fun getForecastByCityName(
        cityName: String, units: String, lang: String
    ): ForecastResponseDto = apiService.getForecastByCityName(
        cityName = cityName, apiKey = apiKey, units = units, lang = lang
    )
}