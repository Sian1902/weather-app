package com.example.weather.data.local

import com.example.weather.data.remote.dto.CurrentWeatherDto
import com.example.weather.data.remote.dto.ForecastResponseDto

data class WeatherCacheModel(
    val current: CurrentWeatherDto,
    val forecast: ForecastResponseDto,
    val cachedAtEpochMs: Long = System.currentTimeMillis()
)