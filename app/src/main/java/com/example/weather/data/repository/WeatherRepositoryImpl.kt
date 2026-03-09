package com.example.weather.data.repository

import com.example.weather.data.remote.CurrentWeatherDto
import com.example.weather.data.remote.ForecastResponseDto
import com.example.weather.data.remote.WeatherRemoteDataSource

class WeatherRepositoryImpl(
    private val remoteDataSource: WeatherRemoteDataSource
) : WeatherRepository {

    override suspend fun getCurrentWeatherByCoordinates(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): CurrentWeatherDto =
        remoteDataSource.getCurrentWeatherByCoordinates(lat, lon, units, lang)

    override suspend fun getCurrentWeatherByCityName(
        cityName: String,
        units: String,
        lang: String
    ): CurrentWeatherDto =
        remoteDataSource.getCurrentWeatherByCityName(cityName, units, lang)

    override suspend fun getForecastByCoordinates(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ForecastResponseDto =
        remoteDataSource.getForecastByCoordinates(lat, lon, units, lang)

    override suspend fun getForecastByCityName(
        cityName: String,
        units: String,
        lang: String
    ): ForecastResponseDto =
        remoteDataSource.getForecastByCityName(cityName, units, lang)
}