package com.example.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SunCard(
    sunriseTime: String,   // "HH:mm"
    sunsetTime : String,   // "HH:mm"
    modifier   : Modifier = Modifier
) {
    // Compute how far through the day we are (0f = sunrise, 1f = sunset)
    val progress = remember(sunriseTime, sunsetTime) {
        computeSunProgress(sunriseTime, sunsetTime)
    }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    DetailCard(modifier = modifier, minHeight = 200.dp) {
        CardHeader(
            iconRes = R.drawable.ic_sunset,
            title   = stringResource(R.string.card_sunset)
        )

        Spacer(Modifier.height(6.dp))

        // Large sunrise time
        Text(
            text       = sunriseTime,
            color      = WeatherColors.TextPrimary,
            fontSize   = 30.sp,
            fontWeight = FontWeight.Light
        )

        Spacer(Modifier.height(12.dp))

        // Arc + sun dot
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            val w = size.width
            val h = size.height

            // Horizontal baseline
            drawLine(
                color       = Color.White.copy(alpha = 0.15f),
                start       = Offset(0f, h * 0.85f),
                end         = Offset(w, h * 0.85f),
                strokeWidth = 1.dp.toPx()
            )

            // Arc path (left = sunrise side, right = sunset side, or flipped for RTL)
            val arcPath = Path().apply {
                moveTo(0f, h * 0.85f)
                cubicTo(w * 0.25f, 0f, w * 0.75f, 0f, w, h * 0.85f)
            }
            drawPath(
                path  = arcPath,
                color = Color.White.copy(alpha = 0.25f),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Sun dot position along the cubic bezier
            // In RTL progress reads right-to-left, so invert
            val t = if (isRtl) 1f - progress else progress
            val sunX = cubicBezierX(t, 0f, w * 0.25f, w * 0.75f, w)
            val sunY = cubicBezierY(t, h * 0.85f, 0f, 0f, h * 0.85f)

            // Glow halo
            drawCircle(
                color  = Color(0xFFFFCC44).copy(alpha = 0.20f),
                radius = 10.dp.toPx(),
                center = Offset(sunX, sunY)
            )
            // Sun dot
            drawCircle(
                color  = Color(0xFFFFCC44).copy(alpha = 0.90f),
                radius = 5.dp.toPx(),
                center = Offset(sunX, sunY)
            )
        }

        Spacer(Modifier.weight(1f))

        // Sunset label at bottom — reuses card_sunset title + time value
        Text(
            text      = "${stringResource(R.string.card_sunset)}: $sunsetTime",
            color     = WeatherColors.TextSecondary,
            fontSize  = 12.sp,
            textAlign = if (isRtl) TextAlign.End else TextAlign.Start,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────

/** Returns 0..1 representing current-time position between sunrise and sunset. */
private fun computeSunProgress(sunriseTime: String, sunsetTime: String): Float {
    return try {
        val fmt      = SimpleDateFormat("HH:mm", Locale.US)
        val rise     = fmt.parse(sunriseTime)?.time ?: return 0.5f
        val set      = fmt.parse(sunsetTime)?.time  ?: return 0.5f
        val nowStr   = SimpleDateFormat("HH:mm", Locale.US).format(Date())
        val now      = fmt.parse(nowStr)?.time ?: return 0.5f
        ((now - rise).toFloat() / (set - rise).toFloat()).coerceIn(0f, 1f)
    } catch (e: Exception) { 0.5f }
}

/** Cubic Bézier X at parameter t. */
private fun cubicBezierX(t: Float, p0x: Float, p1x: Float, p2x: Float, p3x: Float): Float {
    val u = 1f - t
    return u*u*u*p0x + 3*u*u*t*p1x + 3*u*t*t*p2x + t*t*t*p3x
}

/** Cubic Bézier Y at parameter t. */
private fun cubicBezierY(t: Float, p0y: Float, p1y: Float, p2y: Float, p3y: Float): Float {
    val u = 1f - t
    return u*u*u*p0y + 3*u*u*t*p1y + 3*u*t*t*p2y + t*t*t*p3y
}