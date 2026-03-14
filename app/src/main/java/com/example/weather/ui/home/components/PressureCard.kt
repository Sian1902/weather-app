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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun PressureCard(
    pressure : Int,
    modifier : Modifier = Modifier
) {
    val description = when {
        pressure < 980  -> stringResource(R.string.pressure_very_low)
        pressure < 1000 -> stringResource(R.string.pressure_low)
        pressure < 1013 -> stringResource(R.string.pressure_slightly_low)
        pressure < 1020 -> stringResource(R.string.pressure_normal)
        pressure < 1030 -> stringResource(R.string.pressure_slightly_high)
        else            -> stringResource(R.string.pressure_high)
    }

    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(
            iconRes = R.drawable.ic_pressure,
            title = stringResource(R.string.card_pressure)
        )
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text       = "$pressure",
                color      = WeatherColors.TextPrimary,
                fontSize   = 36.sp,
                fontWeight = FontWeight.Light
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text     = stringResource(R.string.pressure_unit),
                color    = WeatherColors.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(100.dp)) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f
                val r = w / 2f

                // Draw Scale Ticks
                for (i in 0..50) {
                    val angle = Math.toRadians(i * 4.8 - 210.0)
                    val tickLen = if (i % 5 == 0) 8.dp.toPx() else 4.dp.toPx()
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f),
                        start = Offset(cx + r * Math.cos(angle).toFloat(), cy + r * Math.sin(angle).toFloat()),
                        end = Offset(cx + (r - tickLen) * Math.cos(angle).toFloat(), cy + (r - tickLen) * Math.sin(angle).toFloat()),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Needle Logic
                val normalized = ((pressure - 950f) / 100f).coerceIn(0f, 1f)
                val needleAngle = Math.toRadians((-210 + normalized * 240).toDouble())
                drawLine(
                    color = Color.White,
                    start = Offset(cx, cy),
                    end = Offset(cx + r * 0.7f * Math.cos(needleAngle).toFloat(), cy + r * 0.7f * Math.sin(needleAngle).toFloat()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(cx, cy))
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = description,
            color = WeatherColors.TextSecondary,
            fontSize = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}