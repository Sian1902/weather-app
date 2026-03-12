package com.example.weather.ui.home

sealed class HomeUiState {
    object Loading : HomeUiState()

    data class Success(
        // ── Location ───────────────────────────────────────────
        val cityName           : String,
        val countryCode        : String,

        // ── Temperature display ────────────────────────────────
        val currentTemp        : String,   // rounded, no unit symbol
        val unitSymbol         : String,   // "°C" or "°F"
        val highTemp           : String,
        val lowTemp            : String,

        // ── Hero subtitle — comes from the API in the requested language ──
        val weatherDescription : String,

        // ── Feels-like card ────────────────────────────────────
        val feelsLike          : String,   // rounded display string
        val actualTemp         : String,   // same as currentTemp, for display
        val feelsLikeRaw       : Double,   // raw value — card uses this for label lookup
        val actualTempRaw      : Double,   // raw value — card uses this for comparison

        // ── UV index card — card resolves label + description via stringResource()
        val uvIndex            : Int,

        // ── Wind card — card resolves Beaufort + cardinal via stringResource()
        val windSpeedBft       : Int,
        val windDeg            : Int,

        // ── Humidity card — card resolves description via stringResource()
        val humidity           : Int,

        // ── Visibility card — card resolves description via stringResource()
        val visibilityKm       : Int,

        // ── Pressure card — card resolves description via stringResource()
        val pressure           : Int,

        // ── Sun card ───────────────────────────────────────────
        val sunriseTime        : String,
        val sunsetTime         : String,

        // ── Moon phase card — card resolves label via stringResource()
        val moonPhaseRaw       : Double,   // 0.0 – 1.0

        val hourlyItems        : List<HourlyItem>,
        val dailyItems         : List<DailyItem>,

        val isFromCache        : Boolean = false,
        val cachedAtEpochMs    : Long    = 0L
    ) : HomeUiState()

    data class Error(val message: String) : HomeUiState()
}

data class HourlyItem(
    val label    : String,   // "NOW" sentinel or "HH:MM"
    val iconCode : String,
    val temp     : String
)

data class DailyItem(
    val day      : String,   // "TODAY" sentinel or short day name e.g. "Mon"
    val date     : String,   // e.g. "3/8"
    val iconCode : String,
    val high     : String,
    val low      : String
)
