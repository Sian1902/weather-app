package com.example.weather.ui.home

sealed class HomeUiState {
    object Loading : HomeUiState()

    data class Success(
        val cityName: String, val countryCode: String,

        val currentTemp: String,
        val unitSymbol: String,
        val highTemp: String, val lowTemp: String,

        val weatherDescription: String,

        val feelsLike: String,
        val actualTemp: String,
        val feelsLikeRaw: Double,
        val actualTempRaw: Double,

        val uvIndex: Int,

        val windSpeedBft: Int, val windDeg: Int,

        val humidity: Int,

        val visibilityKm: Int,

        val pressure: Int,

        val sunriseTime: String, val sunsetTime: String,

        val moonPhaseRaw: Double,

        val hourlyItems: List<HourlyItem>, val dailyItems: List<DailyItem>,

        val isFromCache: Boolean = false, val cachedAtEpochMs: Long = 0L
    ) : HomeUiState()

    data class Error(val message: String) : HomeUiState()
}

data class HourlyItem(
    val label: String,
    val iconCode: String, val temp: String
)

data class DailyItem(
    val day: String,
    val date: String,
    val iconCode: String, val high: String, val low: String
)
