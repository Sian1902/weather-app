package com.example.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors
import kotlin.math.sin

@Composable
fun WindCard(
    windSpeedBft: Int, windDeg: Int, modifier: Modifier = Modifier
) {
    val beaufortDesc = when (windSpeedBft) {
        0 -> stringResource(R.string.bft_0)
        1 -> stringResource(R.string.bft_1)
        2 -> stringResource(R.string.bft_2)
        3 -> stringResource(R.string.bft_3)
        4 -> stringResource(R.string.bft_4)
        5 -> stringResource(R.string.bft_5)
        6 -> stringResource(R.string.bft_6)
        7 -> stringResource(R.string.bft_7)
        8 -> stringResource(R.string.bft_8)
        9 -> stringResource(R.string.bft_9)
        10 -> stringResource(R.string.bft_10)
        11 -> stringResource(R.string.bft_11)
        else -> stringResource(R.string.bft_12)
    }

    val cardinal = windDegToCardinal(windDeg)
    val windDesc = stringResource(R.string.wind_description, cardinal, beaufortDesc)

    DetailCard(modifier = modifier, minHeight = 200.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Air,
                contentDescription = null,
                tint = WeatherColors.TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.card_wind_force).uppercase(),
                color = WeatherColors.TextSecondary,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(130.dp)) {
                drawCompass(windDeg)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = windSpeedBft.toString(),
                    color = WeatherColors.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.wind_unit),
                    color = WeatherColors.TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = windDesc, color = WeatherColors.TextSecondary, fontSize = 11.sp, maxLines = 2
        )
    }
}

private fun DrawScope.drawCompass(windDeg: Int) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val radius = size.minDimension / 2f

    val tickCount = 72
    for (i in 0 until tickCount) {
        val angleRad = Math.toRadians((i * 360.0 / tickCount))
        val isMajor = i % 9 == 0
        val tickLen = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
        val alpha = if (isMajor) 0.6f else 0.22f
        val outerR = radius
        val innerR = radius - tickLen
        drawLine(
            color = Color.White.copy(alpha = alpha),
            start = Offset(
                cx + outerR * sin(angleRad).toFloat(),
                cy - outerR * Math.cos(angleRad).toFloat()
            ),
            end = Offset(
                cx + innerR * sin(angleRad).toFloat(),
                cy - innerR * Math.cos(angleRad).toFloat()
            ),
            strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    val labelR = radius - 20.dp.toPx()
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = 3.dp.toPx(),
        center = Offset(cx, cy - labelR)
    )

    drawCircle(
        color = Color(0xFF1B2638), radius = radius - 16.dp.toPx()
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.10f),
        radius = radius - 16.dp.toPx(),
        style = Stroke(width = 1.dp.toPx())
    )

    val arrowDeg = (windDeg + 180f)
    rotate(degrees = arrowDeg, pivot = Offset(cx, cy)) {
        val shaftEnd = cy - (radius * 0.50f)
        val tailStart = cy + (radius * 0.28f)
        val headLen = 10.dp.toPx()
        val headWidth = 6.dp.toPx()

        drawCircle(
            color = Color.White, radius = 5.dp.toPx(), center = Offset(cx, tailStart)
        )

        drawLine(
            color = Color.White,
            start = Offset(cx, tailStart),
            end = Offset(cx, shaftEnd + headLen),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = Color.White,
            start = Offset(cx, shaftEnd),
            end = Offset(cx - headWidth, shaftEnd + headLen),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = Color.White,
            start = Offset(cx, shaftEnd),
            end = Offset(cx + headWidth, shaftEnd + headLen),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    drawCircle(
        color = Color(0xFF1B2638), radius = 28.dp.toPx()
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.15f),
        radius = 28.dp.toPx(),
        style = Stroke(width = 1.dp.toPx())
    )
}

private fun windDegToCardinal(deg: Int): String {
    val dirs = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return dirs[((deg + 22.5) / 45).toInt() % 8]
}