package com.example.weather.data.remote.mapper

import com.example.weather.data.remote.dto.CurrentWeatherDto
import com.example.weather.data.remote.dto.ForecastResponseDto
import com.example.weather.ui.home.DailyItem
import com.example.weather.ui.home.HomeUiState
import com.example.weather.ui.home.HourlyItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

object WeatherMapper {

    fun toHomeUiState(
        current: CurrentWeatherDto, forecast: ForecastResponseDto, units: String = "metric"
    ): HomeUiState.Success {

        val unitSymbol = if (units == "metric") "°C" else "°F"
        val currentTemp = current.main.temp.roundToInt().toString()
        val feelsLike = current.main.feelsLike.roundToInt().toString()

        // weatherDescription: from API, already in the correct language
        val description =
            current.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: ""

        val todayLabel = simpleDateLabel(current.dt, current.timezone)
        val todayItems = forecast.list.filter {
            simpleDateLabel(it.dt, current.timezone) == todayLabel
        }
        val highTemp =
            (todayItems.maxOfOrNull { it.main.tempMax } ?: current.main.tempMax).roundToInt()
                .toString()
        val lowTemp =
            (todayItems.minOfOrNull { it.main.tempMin } ?: current.main.tempMin).roundToInt()
                .toString()

        return HomeUiState.Success(
            cityName = current.name,
            countryCode = current.sys.country ?: "",
            currentTemp = currentTemp,
            unitSymbol = unitSymbol,
            highTemp = highTemp,
            lowTemp = lowTemp,
            weatherDescription = description,
            feelsLike = feelsLike,
            actualTemp = currentTemp,
            feelsLikeRaw = current.main.feelsLike,
            actualTempRaw = current.main.temp,
            uvIndex = estimateUvIndex(current.clouds.all, current.dt, current.timezone),
            windSpeedBft = msToBft(current.wind.speed),
            windDeg = current.wind.deg,
            humidity = current.main.humidity,
            visibilityKm = (current.visibility / 1000).coerceIn(0, 30),
            pressure = current.main.pressure,
            sunriseTime = formatTime(current.sys.sunrise ?: 0L, current.timezone),
            sunsetTime = formatTime(current.sys.sunset ?: 0L, current.timezone),
            moonPhaseRaw = moonPhaseRaw(current.dt),
            hourlyItems = buildHourlyItems(current, forecast, current.timezone),
            dailyItems = buildDailyItems(forecast, current.timezone)
        )
    }


    private fun buildHourlyItems(
        current: CurrentWeatherDto, forecast: ForecastResponseDto, tzOffsetSeconds: Int
    ): List<HourlyItem> {
        val result = mutableListOf<HourlyItem>()
        result += HourlyItem(
            label = "NOW",
            iconCode = current.weather.firstOrNull()?.icon ?: "01d",
            temp = "${current.main.temp.roundToInt()}°"
        )
        forecast.list.take(7).forEach { item ->
            result += HourlyItem(
                label = formatHour(item.dt, tzOffsetSeconds),
                iconCode = item.weather.firstOrNull()?.icon ?: "01d",
                temp = "${item.main.temp.roundToInt()}°"
            )
        }
        return result
    }

    private fun buildDailyItems(
        forecast: ForecastResponseDto, tzOffsetSeconds: Int
    ): List<DailyItem> {
        val grouped = forecast.list.groupBy { shortDateLabel(it.dt, tzOffsetSeconds) }
        val dayFmt = SimpleDateFormat("EEE", Locale.ENGLISH)
        val dateFmt = SimpleDateFormat("M/d", Locale.ENGLISH)
        return grouped.entries.take(5).mapIndexed { index, (_, items) ->
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = (items.first().dt + tzOffsetSeconds) * 1000L
            }
            DailyItem(
                day = if (index == 0) "TODAY"  // ForecastCard swaps for R.string.day_today
                else dayFmt.format(cal.time),
                date = dateFmt.format(cal.time),
                iconCode = items.groupingBy { it.weather.firstOrNull()?.icon ?: "01d" }.eachCount()
                    .maxByOrNull { it.value }?.key ?: "01d",
                high = "${items.maxOf { it.main.tempMax }.roundToInt()}°",
                low = "${items.minOf { it.main.tempMin }.roundToInt()}°"
            )
        }
    }

    private fun formatTime(epochSeconds: Long, tzOffsetSeconds: Int): String {
        if (epochSeconds == 0L) return "--:--"
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = (epochSeconds + tzOffsetSeconds) * 1000L
        }
        return "%d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }

    private fun formatHour(epochSeconds: Long, tzOffsetSeconds: Int): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = (epochSeconds + tzOffsetSeconds) * 1000L
        }
        return "%d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }

    private fun simpleDateLabel(epochSeconds: Long, tzOffsetSeconds: Int): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = (epochSeconds + tzOffsetSeconds) * 1000L
        }
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
    }

    private fun shortDateLabel(epochSeconds: Long, tzOffsetSeconds: Int): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = (epochSeconds + tzOffsetSeconds) * 1000L
        }
        return "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    fun msToBft(ms: Double): Int = when {
        ms < 0.3 -> 0; ms < 1.6 -> 1; ms < 3.4 -> 2; ms < 5.5 -> 3
        ms < 8.0 -> 4; ms < 10.8 -> 5; ms < 13.9 -> 6; ms < 17.2 -> 7
        ms < 20.8 -> 8; ms < 24.5 -> 9; ms < 28.5 -> 10; ms < 32.7 -> 11
        else -> 12
    }


    private fun estimateUvIndex(cloudCover: Int, epochSeconds: Long, tzOffsetSeconds: Int): Int {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = (epochSeconds + tzOffsetSeconds) * 1000L
        }
        val mult = when (cal.get(Calendar.HOUR_OF_DAY)) {
            in 6..8 -> 0.3
            in 9..10 -> 0.6
            in 11..13 -> 1.0
            in 14..15 -> 0.8
            in 16..17 -> 0.4
            in 18..19 -> 0.1
            else -> 0.0
        }
        return (8 * mult * (1.0 - cloudCover / 100.0 * 0.75)).roundToInt().coerceIn(0, 11)
    }

    fun moonPhaseRaw(epochSeconds: Long): Double {
        val raw = ((epochSeconds - 947182440L) / 86400.0) % 29.53058867
        return (if (raw < 0) raw + 29.53058867 else raw) / 29.53058867
    }
}