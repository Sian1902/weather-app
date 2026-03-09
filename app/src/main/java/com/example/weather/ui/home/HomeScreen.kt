package com.example.weather.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weather.ui.home.components.*
import com.example.weather.ui.theme.WeatherColors

@Composable
fun HomeScreen(
    cityName: String = "Cairo",
    currentTemp: String = "14",
    highTemp: String = "23",
    lowTemp: String = "13",
    weatherDescription: String = "Cloudy",
    feelsLike: String = "13",
    actualTemp: String = "14",
    feelsLikeDescription: String = "Feels similar to actual temperature",
    uvIndex: Int = 1,
    uvLabel: String = "Minimal",
    uvDescription: String = "Almost no risk of sunburn",
    windSpeedBft: Int = 2,
    windDeg: Int = 315,
    windDescription: String = "NW wind, gentle breeze on the face",
    humidity: Int = 76,
    humidityDescription: String = "Fairly humid, dew is likely to form",
    visibilityKm: Int = 30,
    visibilityDescription: String = "Excellent visibility",
    pressure: Int = 1021,
    pressureDescription: String = "Slightly high pressure",
    sunriseTime: String = "6:14",
    sunsetTime: String = "17:58",
    moonPhase: String = "Waning gibbous",
    hourlyItems: List<HourlyItem> = sampleHourly(),
    dailyItems: List<DailyItem> = sampleDaily(),
    onMenuClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onExtendedForecastClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WeatherColors.SkyTop,
                        WeatherColors.SkyMid,
                        WeatherColors.SkyDeep,
                        WeatherColors.SkyBottom
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
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
        ) {
            TopBar(
                cityName    = cityName,
                onMenuClick = onMenuClick,
                onMoreClick = onMoreClick
            )

            HeroSection(
                currentTemp = currentTemp,
                highTemp    = highTemp,
                lowTemp     = lowTemp
            )

            Spacer(modifier = Modifier.height(24.dp))

            HourlyWeatherCard(items = hourlyItems)

            Spacer(modifier = Modifier.height(12.dp))

            ForecastCard(
                items                   = dailyItems,
                onExtendedForecastClick = onExtendedForecastClick
            )


            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier            = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    UvIndexCard(
                        uvIndex     = uvIndex,
                        label       = uvLabel,
                        description = uvDescription,
                        modifier    = Modifier.weight(1f)
                    )
                    FeelsLikeCard(
                        feelsLike   = feelsLike,
                        actualTemp  = actualTemp,
                        description = feelsLikeDescription,
                        modifier    = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WindCard(
                        speedBft    = windSpeedBft,
                        deg         = windDeg,
                        description = windDescription,
                        modifier    = Modifier.weight(1f)
                    )
                    SunCard(
                        sunriseTime = sunriseTime,
                        sunsetTime  = sunsetTime,
                        modifier    = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HumidityCard(
                        humidity    = humidity,
                        description = humidityDescription,
                        modifier    = Modifier.weight(1f)
                    )
                    VisibilityCard(
                        visibilityKm = visibilityKm,
                        description  = visibilityDescription,
                        modifier     = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PressureCard(
                        pressure    = pressure,
                        description = pressureDescription,
                        modifier    = Modifier.weight(1f)
                    )
                    MoonPhaseCard(
                        phase    = moonPhase,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}