package com.example.weather.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.ui.home.components.*
import com.example.weather.ui.theme.WeatherColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    cityName: String,
    countryCode: String,
    currentTemp: String,
    highTemp: String,
    lowTemp: String,
    weatherDescription: String,
    feelsLike: String,
    actualTemp: String,
    feelsLikeDescription: String,
    uvIndex: Int,
    uvLabel: String,
    uvDescription: String,
    windSpeedBft: Int,
    windDeg: Int,
    windDescription: String,
    humidity: Int,
    humidityDescription: String,
    visibilityKm: Int,
    visibilityDescription: String,
    pressure: Int,
    pressureDescription: String,
    sunriseTime: String,
    sunsetTime: String,
    moonPhase: String,
    hourlyItems: List<HourlyItem>,
    dailyItems: List<DailyItem>,
    isRefreshing: Boolean = false,
    isFromCache: Boolean = false,
    cachedAtEpochMs: Long = 0L,
    onRefresh: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onExtendedForecastClick: () -> Unit = {}
) {
    val pullState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh    = onRefresh,
        state        = pullState,
        modifier     = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .statusBarsPadding()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            WeatherColors.CloudOverlay,
                            WeatherColors.SkyTop.copy(alpha = 0f)
                        ),
                        center = Offset(300f, 160f),
                        radius = 800f
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
                cityName    = cityName,
                countryCode = countryCode,      // ← new
                onMenuClick = onMenuClick,
                onMoreClick = onMoreClick
            )

            if (isFromCache) {
                CacheBanner(cachedAtEpochMs = cachedAtEpochMs)
            }

            HeroSection(
                currentTemp = currentTemp,
                highTemp    = highTemp,
                lowTemp     = lowTemp
            )

            Spacer(modifier = Modifier.height(24.dp))

            HourlyWeatherCard(items = hourlyItems)

            Spacer(modifier = Modifier.height(12.dp))

            ForecastCard(
                items                   = dailyItems
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier            = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    UvIndexCard(uvIndex = uvIndex, label = uvLabel, description = uvDescription, modifier = Modifier.weight(1f))
                    FeelsLikeCard(feelsLike = feelsLike, actualTemp = actualTemp, description = feelsLikeDescription, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WindCard(speedBft = windSpeedBft, deg = windDeg, description = windDescription, modifier = Modifier.weight(1f))
                    SunCard(sunriseTime = sunriseTime, sunsetTime = sunsetTime, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HumidityCard(humidity = humidity, description = humidityDescription, modifier = Modifier.weight(1f))
                    VisibilityCard(visibilityKm = visibilityKm, description = visibilityDescription, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PressureCard(pressure = pressure, description = pressureDescription, modifier = Modifier.weight(1f))
                    MoonPhaseCard(phase = moonPhase, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CacheBanner(cachedAtEpochMs: Long) {
    val timeStr = androidx.compose.runtime.remember(cachedAtEpochMs) {
        if (cachedAtEpochMs == 0L) "recently"
        else SimpleDateFormat("HH:mm, MMM d", Locale.getDefault()).format(Date(cachedAtEpochMs))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFA000).copy(alpha = 0.85f))
            .padding(vertical = 6.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = "⚡ Offline — showing data from $timeStr",
            color     = Color.White,
            fontSize  = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}