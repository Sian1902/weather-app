package com.example.weather

import com.example.weather.data.local.cities.CityEntity
import com.example.weather.data.remote.CityDto
import com.example.weather.data.remote.CloudsDto
import com.example.weather.data.remote.CoordDto
import com.example.weather.data.remote.CurrentWeatherDto
import com.example.weather.data.remote.ForecastItemDto
import com.example.weather.data.remote.ForecastResponseDto
import com.example.weather.data.remote.MainDto
import com.example.weather.data.remote.SysDto
import com.example.weather.data.remote.WeatherDescriptionDto
import com.example.weather.data.remote.WindDto
import com.example.weather.data.repository.WeatherResult

/**
 * Shared test fixtures used across all unit tests.
 * Centralised here so a single change propagates everywhere.
 */
object TestFixtures {

    // ── DTOs ──────────────────────────────────────────────────────────────────

    val mainDto = MainDto(
        temp = 22.0,
        feelsLike = 21.0,
        tempMin = 18.0,
        tempMax = 26.0,
        pressure = 1013,
        humidity = 55,
        seaLevel = null,
        grndLevel = null
    )

    val weatherDescDto = WeatherDescriptionDto(
        id = 800,
        main = "Clear",
        description = "clear sky",
        icon = "01d"
    )

    val sysDto = SysDto(country = "EG", sunrise = 1_700_000_000L, sunset = 1_700_043_200L)

    val currentWeatherDto = CurrentWeatherDto(
        coord = CoordDto(lon = 31.23, lat = 30.06),
        weather = listOf(weatherDescDto),
        main = mainDto,
        visibility = 10_000,
        wind = WindDto(speed = 3.0, deg = 90, gust = null),
        clouds = CloudsDto(all = 0),
        dt = 1_700_020_000L,
        sys = sysDto,
        timezone = 7200,
        name = "Cairo"
    )

    val forecastItemDto = ForecastItemDto(
        dt = 1_700_020_000L,
        main = mainDto,
        weather = listOf(weatherDescDto),
        clouds = CloudsDto(all = 0),
        wind = WindDto(speed = 3.0, deg = 90, gust = null),
        visibility = 10_000,
        pop = 0.0,
        rain = null,
        snow = null,
        dtTxt = "2023-11-15 12:00:00"
    )

    val forecastResponseDto = ForecastResponseDto(
        list = listOf(forecastItemDto),
        city = CityDto(
            id = 360630,
            name = "Cairo",
            coord = CoordDto(lon = 31.23, lat = 30.06),
            country = "EG",
            timezone = 7200,
            sunrise = 1_700_000_000L,
            sunset = 1_700_043_200L
        )
    )

    val liveResult   = WeatherResult.Live(currentWeatherDto, forecastResponseDto)
    val cachedResult = WeatherResult.Cached(currentWeatherDto, forecastResponseDto, 1_700_000_000L)

    // ── Cities ────────────────────────────────────────────────────────────────

    fun cityEntity(
        id                : Int     = 1,
        name              : String  = "Cairo",
        lat               : Double  = 30.06,
        lon               : Double  = 31.23,
        isDefault         : Boolean = false,
        isCurrentLocation : Boolean = false
    ) = CityEntity(
        id = id,
        name = name,
        lat = lat,
        lon = lon,
        isDefault = isDefault,
        isCurrentLocation = isCurrentLocation
    )
}