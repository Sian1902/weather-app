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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun WindCard(
    speedBft: Int,
    deg: Int,
    description: String,
    modifier: Modifier = Modifier
) {
    DetailCard(modifier = modifier, minHeight = 200.dp) {
        CardHeader(iconRes = R.drawable.ic_wind, title = "Wind force")
        Spacer(modifier = Modifier.height(8.dp))
        WindCompass(
            speedBft = speedBft,
            deg      = deg,
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, color = WeatherColors.TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun WindCompass(speedBft: Int, deg: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r  = size.minDimension / 2f

            // Tick ring
            for (i in 0 until 72) {
                val angle   = Math.toRadians(i * 5.0)
                val tickLen = if (i % 9 == 0) 10.dp.toPx() else 5.dp.toPx()
                val outerR  = r - 2.dp.toPx()
                val innerR  = outerR - tickLen
                drawLine(
                    color       = Color.White.copy(alpha = 0.5f),
                    start       = Offset(cx + outerR * Math.cos(angle).toFloat(), cy + outerR * Math.sin(angle).toFloat()),
                    end         = Offset(cx + innerR * Math.cos(angle).toFloat(), cy + innerR * Math.sin(angle).toFloat()),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Inner circle
            drawCircle(
                color  = Color(0xFF1E3A5F).copy(alpha = 0.6f),
                radius = r * 0.55f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color  = Color.White.copy(alpha = 0.2f),
                radius = r * 0.55f,
                center = Offset(cx, cy),
                style  = Stroke(1.dp.toPx())
            )

            // Arrow
            val arrowAngle = Math.toRadians(deg.toDouble() - 90)
            val arrowLen   = r * 0.45f
            val arrowEnd   = Offset(
                cx + arrowLen * Math.cos(arrowAngle).toFloat(),
                cy + arrowLen * Math.sin(arrowAngle).toFloat()
            )
            drawLine(
                color       = Color.White,
                start       = Offset(cx, cy),
                end         = arrowEnd,
                strokeWidth = 2.5.dp.toPx(),
                cap         = StrokeCap.Round
            )
            // Arrowhead
            val headLen    = 12.dp.toPx()
            val headAngle1 = arrowAngle + Math.toRadians(150.0)
            val headAngle2 = arrowAngle - Math.toRadians(150.0)
            drawLine(
                color       = Color.White,
                start       = arrowEnd,
                end         = Offset(arrowEnd.x + headLen * Math.cos(headAngle1).toFloat(), arrowEnd.y + headLen * Math.sin(headAngle1).toFloat()),
                strokeWidth = 2.dp.toPx(),
                cap         = StrokeCap.Round
            )
            drawLine(
                color       = Color.White,
                start       = arrowEnd,
                end         = Offset(arrowEnd.x + headLen * Math.cos(headAngle2).toFloat(), arrowEnd.y + headLen * Math.sin(headAngle2).toFloat()),
                strokeWidth = 2.dp.toPx(),
                cap         = StrokeCap.Round
            )

            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(cx, cy))
        }

        // Bft label in centre
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$speedBft", color = WeatherColors.TextPrimary, fontSize = 20.sp)
            Text(text = "Bft",      color = WeatherColors.TextSecondary, fontSize = 11.sp)
        }
    }
}