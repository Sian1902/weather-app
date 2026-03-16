package com.example.weather.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.home.components.FeelsLikeCard
import com.example.weather.ui.home.components.ForecastCard
import com.example.weather.ui.home.components.HeroSection
import com.example.weather.ui.home.components.HourlyWeatherCard
import com.example.weather.ui.home.components.HumidityCard
import com.example.weather.ui.home.components.MoonPhaseCard
import com.example.weather.ui.home.components.PressureCard
import com.example.weather.ui.home.components.SunCard
import com.example.weather.ui.home.components.TopBar
import com.example.weather.ui.home.components.UvIndexCard
import com.example.weather.ui.home.components.VisibilityCard
import com.example.weather.ui.home.components.WindCard
import com.example.weather.ui.theme.WeatherColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    cityName: String,
    countryCode: String,
    currentTemp: String,
    unitSymbol: String,
    highTemp: String,
    lowTemp: String,
    weatherDescription: String,
    feelsLike: String,
    actualTemp: String,
    feelsLikeRaw: Double,
    actualTempRaw: Double,
    uvIndex: Int,
    windSpeedBft: Int,
    windDeg: Int,
    humidity: Int,
    visibilityKm: Int,
    pressure: Int,
    sunriseTime: String,
    sunsetTime: String,
    moonPhaseRaw: Double,
    hourlyItems: List<HourlyItem>,
    dailyItems: List<DailyItem>,
    units: String = "metric",
    isRefreshing: Boolean = false,
    isFromCache: Boolean = false,
    cachedAtEpochMs: Long = 0L,
    pageCount: Int = 1,
    currentPage: Int = 0,
    onRefresh: () -> Unit = {},
    onUnitsToggle: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onExtendedForecastClick: () -> Unit = {}
) {
    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullState,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .statusBarsPadding()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            WeatherColors.CloudOverlay, WeatherColors.SkyTop.copy(alpha = 0f)
                        ), center = Offset(300f, 160f), radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            TopBar(
                cityName = cityName,
                countryCode = countryCode,
                units = units,
                pageCount = pageCount,
                currentPage = currentPage,
                onUnitsToggle = onUnitsToggle,
                onSettingsClick = onSettingsClick,
                onMenuClick = onMenuClick
            )

            if (isFromCache) CacheBanner(cachedAtEpochMs = cachedAtEpochMs)

            HeroSection(
                temp = currentTemp,
                unit = unitSymbol,
                high = highTemp,
                low = lowTemp,
                description = weatherDescription
            )

            Spacer(Modifier.height(24.dp))
            HourlyWeatherCard(items = hourlyItems)
            Spacer(Modifier.height(12.dp))
            ForecastCard(items = dailyItems, onExtendedForecastClick = onExtendedForecastClick)
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    UvIndexCard(uvIndex, Modifier.weight(1f))
                    FeelsLikeCard(
                        feelsLike,
                        actualTemp,
                        feelsLikeRaw,
                        actualTempRaw,
                        Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WindCard(windSpeedBft, windDeg, Modifier.weight(1f))
                    SunCard(sunriseTime, sunsetTime, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HumidityCard(humidity, Modifier.weight(1f))
                    VisibilityCard(visibilityKm, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PressureCard(pressure, Modifier.weight(1f))
                    MoonPhaseCard(moonPhaseRaw, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CacheBanner(cachedAtEpochMs: Long) {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0] ?: Locale.getDefault()
    val recentlyLabel = stringResource(R.string.cache_recently)

    val timeStr = remember(cachedAtEpochMs, locale) {
        if (cachedAtEpochMs == 0L) recentlyLabel
        else SimpleDateFormat("HH:mm, MMM d", locale).format(Date(cachedAtEpochMs))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFA000).copy(alpha = 0.85f))
            .padding(vertical = 6.dp, horizontal = 16.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.offline_banner, timeStr),
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}