package com.example.weather.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun FeelsLikeCard(
    feelsLike: String,
    actualTemp: String,
    description: String,
    modifier: Modifier = Modifier
) {
    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(iconRes = R.drawable.ic_thermometer, title = "Feels like")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text       = "$feelsLike°",
            color      = WeatherColors.TextPrimary,
            fontSize   = 36.sp,
            fontWeight = FontWeight.Light
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text     = "Actual temperature: $actualTemp°",
            color    = WeatherColors.TextSecondary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text     = description,
            color    = WeatherColors.TextSecondary,
            fontSize = 11.sp
        )
    }
}