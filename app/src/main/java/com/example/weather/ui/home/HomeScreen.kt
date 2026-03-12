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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.home.components.*
import com.example.weather.ui.theme.WeatherColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    cityName           : String,
    countryCode        : String,
    currentTemp        : String,
    unitSymbol         : String,
    highTemp           : String,
    lowTemp            : String,
    weatherDescription : String,
    feelsLike          : String,
    actualTemp         : String,
    feelsLikeRaw       : Double,
    actualTempRaw      : Double,
    uvIndex            : Int,
    windSpeedBft       : Int,
    windDeg            : Int,
    humidity           : Int,
    visibilityKm       : Int,
    pressure           : Int,
    sunriseTime        : String,
    sunsetTime         : String,
    moonPhaseRaw       : Double,
    hourlyItems        : List<HourlyItem>,
    dailyItems         : List<DailyItem>,
    units              : String  = "metric",
    isRefreshing       : Boolean = false,
    isFromCache        : Boolean = false,
    cachedAtEpochMs    : Long    = 0L,
    onRefresh          : () -> Unit = {},
    onUnitsToggle      : () -> Unit = {},
    onSettingsClick    : () -> Unit = {},
    onMenuClick        : () -> Unit = {},
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
                        colors = listOf(WeatherColors.CloudOverlay, WeatherColors.SkyTop.copy(alpha = 0f)),
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
                cityName        = cityName,
                countryCode     = countryCode,
                units           = units,
                onUnitsToggle   = onUnitsToggle,
                onSettingsClick = onSettingsClick,
                onMenuClick     = onMenuClick
            )

            if (isFromCache) CacheBanner(cachedAtEpochMs = cachedAtEpochMs)

            HeroSection(
                currentTemp = currentTemp,
                unitSymbol  = unitSymbol,
                highTemp    = highTemp,
                lowTemp     = lowTemp
            )

            Spacer(modifier = Modifier.height(24.dp))

            HourlyWeatherCard(items = hourlyItems)

            Spacer(modifier = Modifier.height(12.dp))

            ForecastCard(items = dailyItems, onExtendedForecastClick = onExtendedForecastClick)

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier            = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    UvIndexCard(uvIndex = uvIndex, modifier = Modifier.weight(1f))
                    FeelsLikeCard(
                        feelsLike     = feelsLike,
                        actualTemp    = actualTemp,
                        feelsLikeRaw  = feelsLikeRaw,
                        actualTempRaw = actualTempRaw,
                        modifier      = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WindCard(speedBft = windSpeedBft, deg = windDeg, modifier = Modifier.weight(1f))
                    SunCard(sunriseTime = sunriseTime, sunsetTime = sunsetTime, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HumidityCard(humidity = humidity, modifier = Modifier.weight(1f))
                    VisibilityCard(visibilityKm = visibilityKm, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PressureCard(pressure = pressure, modifier = Modifier.weight(1f))
                    MoonPhaseCard(moonPhaseRaw = moonPhaseRaw, modifier = Modifier.weight(1f))
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
            text      = stringResource(R.string.offline_banner, timeStr),
            color     = Color.White,
            fontSize  = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}