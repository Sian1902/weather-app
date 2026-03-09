package com.example.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun PressureCard(
    pressure: Int,
    description: String,
    modifier: Modifier = Modifier
) {
    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(iconRes = R.drawable.ic_pressure, title = "Pressure")
        Spacer(modifier = Modifier.height(8.dp))
        PressureGauge(
            pressure = pressure,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, color = WeatherColors.TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun PressureGauge(pressure: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r  = size.minDimension / 2f - 4.dp.toPx()

            for (i in 0 until 60) {
                val angle   = Math.toRadians(i * 6.0 - 150.0)
                val tickLen = if (i % 5 == 0) 8.dp.toPx() else 4.dp.toPx()
                val outerR  = r
                val innerR  = r - tickLen
                drawLine(
                    color       = Color.White.copy(alpha = 0.4f),
                    start       = Offset(cx + outerR * Math.cos(angle).toFloat(), cy + outerR * Math.sin(angle).toFloat()),
                    end         = Offset(cx + innerR * Math.cos(angle).toFloat(), cy + innerR * Math.sin(angle).toFloat()),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val normalized  = ((pressure - 950f) / 100f).coerceIn(0f, 1f)
            val needleAngle = Math.toRadians((-120 + normalized * 240).toDouble())
            val needleLen   = r * 0.7f
            drawLine(
                color       = Color.White,
                start       = Offset(cx, cy),
                end         = Offset(
                    cx + needleLen * Math.cos(needleAngle).toFloat(),
                    cy + needleLen * Math.sin(needleAngle).toFloat()
                ),
                strokeWidth = 2.dp.toPx(),
                cap         = StrokeCap.Round
            )
            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(cx, cy))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "$pressure", color = WeatherColors.TextPrimary, fontSize = 16.sp)
            Text(text = "hPa",       color = WeatherColors.TextSecondary, fontSize = 10.sp)
        }
    }
}