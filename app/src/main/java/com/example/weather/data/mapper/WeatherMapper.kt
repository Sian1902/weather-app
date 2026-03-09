package com.example.weather.data.mapper

import com.example.weather.data.remote.CurrentWeatherDto
import com.example.weather.data.remote.ForecastItemDto
import com.example.weather.data.remote.ForecastResponseDto
import com.example.weather.ui.home.DailyItem
import com.example.weather.ui.home.HomeUiState
import com.example.weather.ui.home.HourlyItem
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object WeatherMapper {

    fun toHomeUiState(
        current: CurrentWeatherDto,
        forecast: ForecastResponseDto
    ): HomeUiState.Success {

        val tz = TimeZone.getTimeZone("UTC").also {
            // Shift to city timezone using offset seconds
        }

        /* ── current weather ── */
        val currentTemp  = current.main.temp.roundToInt().toString()
        val feelsLike    = current.main.feelsLike.roundToInt().toString()
        val description  = current.weather.firstOrNull()?.description
            ?.replaceFirstChar { it.uppercase() } ?: ""

        /* ── today's high / low from forecast list ── */
        val todayLabel   = simpleDateLabel(current.dt, current.timezone)
        val todayItems   = forecast.list.filter {
            simpleDateLabel(it.dt, current.timezone) == todayLabel
        }
        val highTemp = (todayItems.maxOfOrNull { it.main.tempMax }
            ?: current.main.tempMax).roundToInt().toString()
        val lowTemp  = (todayItems.minOfOrNull { it.main.tempMin }
            ?: current.main.tempMin).roundToInt().toString()

        /* ── wind ── */
        val bft          = msToBft(current.wind.speed)
        val windDir      = degToCardinal(current.wind.deg)
        val windDesc     = "$windDir wind, ${bftDescription(bft)}"

        /* ── humidity ── */
        val humidity     = current.main.humidity
        val humidityDesc = humidityDescription(humidity)

        /* ── visibility ── */
        val visKm        = (current.visibility / 1000).coerceIn(0, 30)
        val visDesc      = visibilityDescription(visKm)

        /* ── pressure ── */
        val pressure     = current.main.pressure
        val pressureDesc = pressureDescription(pressure)

        /* ── sunrise / sunset ── */
        val sunrise = formatTime(current.sys.sunrise ?: 0L, current.timezone)
        val sunset  = formatTime(current.sys.sunset  ?: 0L, current.timezone)

        /* ── moon phase ── */
        val moonPhase = moonPhaseLabel(current.dt)

        /* ── UV (placeholder — free OWM /weather has no UV field) ── */
        val uvIndex = estimateUvIndex(current.clouds.all, current.dt, current.timezone)
        val uvLabel = uvLabel(uvIndex)
        val uvDesc  = uvDescription(uvIndex)

        /* ── hourly (next 8 slots = 24 h) ── */
        val hourlyItems = buildHourlyItems(current, forecast, current.timezone)

        /* ── daily (5 days) ── */
        val dailyItems = buildDailyItems(forecast, current.timezone)

        return HomeUiState.Success(
            cityName             = current.name,
            currentTemp          = currentTemp,
            highTemp             = highTemp,
            lowTemp              = lowTemp,
            weatherDescription   = description,
            feelsLike            = feelsLike,
            actualTemp           = currentTemp,
            feelsLikeDescription = feelsLikeDescription(
                current.main.feelsLike, current.main.temp
            ),
            uvIndex              = uvIndex,
            uvLabel              = uvLabel,
            uvDescription        = uvDesc,
            windSpeedBft         = bft,
            windDeg              = current.wind.deg,
            windDescription      = windDesc,
            humidity             = humidity,
            humidityDescription  = humidityDesc,
            visibilityKm         = visKm,
            visibilityDescription= visDesc,
            pressure             = pressure,
            pressureDescription  = pressureDesc,
            sunriseTime          = sunrise,
            sunsetTime           = sunset,
            moonPhase            = moonPhase,
            hourlyItems          = hourlyItems,
            dailyItems           = dailyItems
        )
    }

    /* ────────────────────────────────────────────
       Hourly: first entry = "Now" (current), then
       up to 7 forecast slots
    ──────────────────────────────────────────── */
    private fun buildHourlyItems(
        current: CurrentWeatherDto,
        forecast: ForecastResponseDto,
        tzOffsetSeconds: Int
    ): List<HourlyItem> {
        val result = mutableListOf<HourlyItem>()

        // "Now" row uses current weather
        result += HourlyItem(
            label    = "Now",
            iconCode = current.weather.firstOrNull()?.icon ?: "01d",
            temp     = "${current.main.temp.roundToInt()}°"
        )

        // Next slots from forecast list (3-hour intervals)
        forecast.list.take(7).forEach { item ->
            result += HourlyItem(
                label    = formatHour(item.dt, tzOffsetSeconds),
                iconCode = item.weather.firstOrNull()?.icon ?: "01d",
                temp     = "${item.main.temp.roundToInt()}°"
            )
        }
        return result
    }

    /* ────────────────────────────────────────────
       Daily: group forecast items by day, pick
       max/min temp and most common icon
    ──────────────────────────────────────────── */
    private fun buildDailyItems(
        forecast: ForecastResponseDto,
        tzOffsetSeconds: Int
    ): List<DailyItem> {
        // Group by date string "M/d"
        val grouped = forecast.list.groupBy { item ->
            shortDateLabel(item.dt, tzOffsetSeconds)
        }

        val dayFmt  = SimpleDateFormat("EEE", Locale.ENGLISH)
        val dateFmt = SimpleDateFormat("M/d",  Locale.ENGLISH)

        return grouped.entries.take(5).mapIndexed { index, (_, items) ->
            val firstDt = items.first().dt
            val epochMs = (firstDt + tzOffsetSeconds) * 1000L
            val cal     = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = epochMs
            }

            val dayName  = if (index == 0) "Today" else dayFmt.format(cal.time)
            val dateStr  = dateFmt.format(cal.time)
            val high     = items.maxOf { it.main.tempMax }.roundToInt()
            val low      = items.minOf { it.main.tempMin }.roundToInt()
            val icon     = items.groupingBy { it.weather.firstOrNull()?.icon ?: "01d" }
                .eachCount().maxByOrNull { it.value }?.key ?: "01d"

            DailyItem(
                day      = dayName,
                date     = dateStr,
                iconCode = icon,
                high     = "$high°",
                low      = "$low°"
            )
        }
    }

    /* ── helpers ── */

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

    /** Beaufort scale from m/s */
    private fun msToBft(ms: Double): Int = when {
        ms < 0.3  -> 0
        ms < 1.6  -> 1
        ms < 3.4  -> 2
        ms < 5.5  -> 3
        ms < 8.0  -> 4
        ms < 10.8 -> 5
        ms < 13.9 -> 6
        ms < 17.2 -> 7
        ms < 20.8 -> 8
        ms < 24.5 -> 9
        ms < 28.5 -> 10
        ms < 32.7 -> 11
        else      -> 12
    }

    private fun bftDescription(bft: Int): String = when (bft) {
        0    -> "calm"
        1    -> "light air"
        2    -> "gentle breeze on the face"
        3    -> "leaves and twigs in motion"
        4    -> "moderate breeze"
        5    -> "small trees sway"
        6    -> "large branches in motion"
        7    -> "whole trees in motion"
        8    -> "twigs break off"
        9    -> "slight structural damage"
        10   -> "trees uprooted"
        11   -> "widespread structural damage"
        else -> "hurricane force"
    }

    private fun degToCardinal(deg: Int): String {
        val dirs = arrayOf("N","NNE","NE","ENE","E","ESE","SE","SSE",
            "S","SSW","SW","WSW","W","WNW","NW","NNW")
        return dirs[((deg + 11.25) / 22.5).toInt() % 16]
    }

    private fun humidityDescription(h: Int): String = when {
        h < 30 -> "Very dry air"
        h < 50 -> "Comfortable humidity"
        h < 60 -> "Slightly humid"
        h < 75 -> "Fairly humid, dew is likely to form"
        h < 90 -> "High humidity"
        else   -> "Very high humidity"
    }

    private fun visibilityDescription(km: Int): String = when {
        km >= 20 -> "Excellent visibility"
        km >= 10 -> "Good visibility"
        km >= 4  -> "Moderate visibility"
        km >= 1  -> "Poor visibility"
        else     -> "Very poor visibility"
    }

    private fun pressureDescription(hPa: Int): String = when {
        hPa < 980  -> "Very low pressure, stormy"
        hPa < 1000 -> "Low pressure"
        hPa < 1013 -> "Slightly low pressure"
        hPa < 1020 -> "Normal pressure"
        hPa < 1030 -> "Slightly high pressure"
        else       -> "High pressure, stable"
    }

    private fun feelsLikeDescription(feels: Double, actual: Double): String {
        val diff = feels - actual
        return when {
            diff > 3  -> "Feels warmer than actual temperature"
            diff < -3 -> "Feels colder than actual temperature"
            else      -> "Feels similar to actual temperature"
        }
    }

    /**
     * Rough UV estimate — OWM free tier exposes UV only via the
     * One Call API (requires subscription). We approximate from
     * cloud cover and hour of day until One Call is wired.
     */
    private fun estimateUvIndex(cloudCover: Int, epochSeconds: Long, tzOffsetSeconds: Int): Int {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = (epochSeconds + tzOffsetSeconds) * 1000L
        }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        // Peak UV mid-day, zero at night
        val timeMultiplier = when (hour) {
            in 6..8   -> 0.3
            in 9..10  -> 0.6
            in 11..13 -> 1.0
            in 14..15 -> 0.8
            in 16..17 -> 0.4
            in 18..19 -> 0.1
            else      -> 0.0
        }
        val cloudMultiplier = 1.0 - cloudCover / 100.0 * 0.75
        return (8 * timeMultiplier * cloudMultiplier).roundToInt().coerceIn(0, 11)
    }

    private fun uvLabel(uv: Int): String = when {
        uv <= 2  -> "Minimal"
        uv <= 5  -> "Moderate"
        uv <= 7  -> "High"
        uv <= 10 -> "Very High"
        else     -> "Extreme"
    }

    private fun uvDescription(uv: Int): String = when {
        uv <= 2  -> "Almost no risk of sunburn"
        uv <= 5  -> "Some risk, wear sunscreen"
        uv <= 7  -> "High risk, protect yourself"
        uv <= 10 -> "Very high risk, avoid midday sun"
        else     -> "Extreme risk, stay indoors"
    }

    /**
     * Approximate moon phase from epoch using 29.53-day synodic month.
     * Reference new moon: Jan 6 2000 18:14 UTC → epoch 947182440
     */
    private fun moonPhaseLabel(epochSeconds: Long): String {
        val synodicMonth = 29.53058867
        val newMoonRef   = 947182440L
        val daysSince    = (epochSeconds - newMoonRef) / 86400.0
        val phase        = ((daysSince % synodicMonth + synodicMonth) % synodicMonth) / synodicMonth
        return when {
            phase < 0.03 || phase >= 0.97 -> "New Moon"
            phase < 0.22 -> "Waxing Crescent"
            phase < 0.28 -> "First Quarter"
            phase < 0.47 -> "Waxing Gibbous"
            phase < 0.53 -> "Full Moon"
            phase < 0.72 -> "Waning Gibbous"
            phase < 0.78 -> "Last Quarter"
            else         -> "Waning Crescent"
        }
    }
}