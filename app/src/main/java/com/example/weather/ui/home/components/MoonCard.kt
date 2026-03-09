package com.example.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun MoonPhaseCard(
    phase: String,
    modifier: Modifier = Modifier
) {
    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(iconRes = R.drawable.ic_moon, title = "Moon phase")
        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r  = size.minDimension / 2f

            drawCircle(
                color  = WeatherColors.MoonSurface,
                radius = r,
                center = Offset(cx, cy)
            )
            drawCircle(
                color  = WeatherColors.CardBgMoon.copy(alpha = 0.7f),
                radius = r,
                center = Offset(cx - r * 0.35f, cy)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = phase, color = WeatherColors.TextSecondary, fontSize = 12.sp)
    }
}