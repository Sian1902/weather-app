package com.example.weather.data.local

import com.example.weather.data.remote.CurrentWeatherDto
import com.example.weather.data.remote.ForecastResponseDto

data class WeatherCacheModel(
    val current: CurrentWeatherDto,
    val forecast: ForecastResponseDto,
    val cachedAtEpochMs: Long = System.currentTimeMillis()
)