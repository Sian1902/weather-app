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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors
import kotlin.math.cos
import kotlin.math.sin

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

    // Needle angle: maps 950–1050 hPa → -225° to 45° (270° sweep, starting bottom-left)
    val normalized   = ((pressure - 950f) / 100f).coerceIn(0f, 1f)

    DetailCard(modifier = modifier, minHeight = 200.dp) {
        CardHeader(
            iconRes = R.drawable.ic_pressure,
            title   = stringResource(R.string.card_pressure)
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawGauge(normalized)
            }
            // Value + unit centred over the gauge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = "$pressure",
                    color      = WeatherColors.TextPrimary,
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text     = stringResource(R.string.pressure_unit),
                    color    = WeatherColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text     = description,
            color    = WeatherColors.TextSecondary,
            fontSize = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Gauge drawing ──────────────────────────────────────────────────────────

private fun DrawScope.drawGauge(normalized: Float) {
    val cx     = size.width  / 2f
    val cy     = size.height / 2f * 1.05f          // shift centre slightly down
    val radius = (size.minDimension / 2f) * 0.82f

    // Arc sweep: 270° starting from -225° (bottom-left) to 45° (bottom-right)
    val startAngle = -225f
    val sweepAngle = 270f
    val tickCount  = 60                             // one tick per ~4.5°

    for (i in 0..tickCount) {
        val fraction = i.toFloat() / tickCount
        val angleDeg = startAngle + fraction * sweepAngle
        val angleRad = Math.toRadians(angleDeg.toDouble())

        val isMajor   = i % 5 == 0
        val isNeedle  = fraction <= normalized      // ticks "before" needle are brighter

        val tickLen   = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
        val alpha     = when {
            isNeedle && isMajor -> 0.85f
            isNeedle            -> 0.45f
            isMajor             -> 0.30f
            else                -> 0.15f
        }
        val strokeW   = if (isMajor) 2.dp.toPx() else 1.dp.toPx()

        val outerX = cx + radius * cos(angleRad).toFloat()
        val outerY = cy + radius * sin(angleRad).toFloat()
        val innerX = cx + (radius - tickLen) * cos(angleRad).toFloat()
        val innerY = cy + (radius - tickLen) * sin(angleRad).toFloat()

        drawLine(
            color       = Color.White.copy(alpha = alpha),
            start       = Offset(outerX, outerY),
            end         = Offset(innerX, innerY),
            strokeWidth = strokeW,
            cap         = StrokeCap.Round
        )
    }

    // Needle — bright white line from centre to rim
    val needleAngleDeg = startAngle + normalized * sweepAngle
    val needleRad      = Math.toRadians(needleAngleDeg.toDouble())
    val needleTipX     = cx + (radius - 2.dp.toPx()) * cos(needleRad).toFloat()
    val needleTipY     = cy + (radius - 2.dp.toPx()) * sin(needleRad).toFloat()

    drawLine(
        color       = Color.White,
        start       = Offset(cx, cy),
        end         = Offset(needleTipX, needleTipY),
        strokeWidth = 2.dp.toPx(),
        cap         = StrokeCap.Round
    )
    // Needle base dot
    drawCircle(
        color  = Color.White,
        radius = 3.5.dp.toPx(),
        center = Offset(cx, cy)
    )
}