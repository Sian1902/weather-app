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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.data.mapper.WeatherMapper
import com.example.weather.ui.theme.WeatherColors

/**
 * Wind card.
 * Resolves Beaufort description from strings.xml; compass direction stays as
 * standard abbreviation (N / NE / NW / …) which is universal across languages.
 */
@Composable
fun WindCard(
    speedBft : Int,
    deg      : Int,
    modifier : Modifier = Modifier
) {
    val bftDesc = when (speedBft) {
        0    -> stringResource(R.string.bft_0)
        1    -> stringResource(R.string.bft_1)
        2    -> stringResource(R.string.bft_2)
        3    -> stringResource(R.string.bft_3)
        4    -> stringResource(R.string.bft_4)
        5    -> stringResource(R.string.bft_5)
        6    -> stringResource(R.string.bft_6)
        7    -> stringResource(R.string.bft_7)
        8    -> stringResource(R.string.bft_8)
        9    -> stringResource(R.string.bft_9)
        10   -> stringResource(R.string.bft_10)
        11   -> stringResource(R.string.bft_11)
        else -> stringResource(R.string.bft_12)
    }
    // cardinal direction (N, NW, …) is intentionally kept as English abbreviation —
    // these are internationally standardised symbols used on every compass worldwide.
    val cardinal    = WeatherMapper.degToCardinal(deg)
    val description = stringResource(R.string.wind_description, cardinal, bftDesc)

    DetailCard(modifier = modifier, minHeight = 200.dp) {
        CardHeader(iconRes = R.drawable.ic_wind, title = stringResource(R.string.card_wind_force))
        Spacer(Modifier.height(8.dp))
        WindCompass(
            speedBft = speedBft,
            deg      = deg,
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(8.dp))
        Text(text = description, color = WeatherColors.TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun WindCompass(speedBft: Int, deg: Int, modifier: Modifier = Modifier) {
    val bftLabel = stringResource(R.string.wind_unit)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f; val cy = size.height / 2f
            val r  = size.minDimension / 2f
            // Tick marks
            for (i in 0 until 72) {
                val angle   = Math.toRadians(i * 5.0)
                val tickLen = if (i % 9 == 0) 10.dp.toPx() else 5.dp.toPx()
                val outer   = r - 2.dp.toPx(); val inner = outer - tickLen
                drawLine(
                    color       = Color.White.copy(alpha = 0.5f),
                    start       = Offset(cx + outer * Math.cos(angle).toFloat(), cy + outer * Math.sin(angle).toFloat()),
                    end         = Offset(cx + inner * Math.cos(angle).toFloat(), cy + inner * Math.sin(angle).toFloat()),
                    strokeWidth = 1.dp.toPx()
                )
            }
            // Inner circle
            drawCircle(color = Color(0xFF1E3A5F).copy(alpha = 0.6f), radius = r * 0.55f, center = Offset(cx, cy))
            drawCircle(color = Color.White.copy(alpha = 0.2f),        radius = r * 0.55f, center = Offset(cx, cy), style = Stroke(1.dp.toPx()))
            // Needle
            val arrowAngle = Math.toRadians(deg - 90.0)
            val arrowLen   = r * 0.45f
            val tip = Offset(cx + arrowLen * Math.cos(arrowAngle).toFloat(), cy + arrowLen * Math.sin(arrowAngle).toFloat())
            drawLine(color = Color.White, start = Offset(cx, cy), end = tip, strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
            listOf(150.0, -150.0).forEach { a ->
                val ha = arrowAngle + Math.toRadians(a)
                drawLine(
                    color = Color.White, start = tip,
                    end   = Offset(tip.x + 12.dp.toPx() * Math.cos(ha).toFloat(), tip.y + 12.dp.toPx() * Math.sin(ha).toFloat()),
                    strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round
                )
            }
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(cx, cy))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$speedBft", color = WeatherColors.TextPrimary,  fontSize = 20.sp)
            Text(text = bftLabel,   color = WeatherColors.TextSecondary, fontSize = 11.sp)
        }
    }
}