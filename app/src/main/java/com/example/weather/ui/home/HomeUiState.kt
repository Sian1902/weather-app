package com.example.weather.ui.home

import com.example.weather.ui.home.HourlyItem
import com.example.weather.ui.home.DailyItem

sealed class HomeUiState {
    object Loading : HomeUiState()

    data class Success(
        val cityName: String,
        val currentTemp: String,
        val highTemp: String,
        val lowTemp: String,
        val weatherDescription: String,

        val feelsLike: String,
        val actualTemp: String,
        val feelsLikeDescription: String,

        val uvIndex: Int,
        val uvLabel: String,
        val uvDescription: String,

        val windSpeedBft: Int,
        val windDeg: Int,
        val windDescription: String,

        val humidity: Int,
        val humidityDescription: String,

        val visibilityKm: Int,
        val visibilityDescription: String,

        val pressure: Int,
        val pressureDescription: String,

        val sunriseTime: String,
        val sunsetTime: String,

        val moonPhase: String,

        val hourlyItems: List<HourlyItem>,
        val dailyItems: List<DailyItem>
    ) : HomeUiState()

    data class Error(val message: String) : HomeUiState()
}