package com.example.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun SunCard(
    sunriseTime: String,
    sunsetTime: String,
    modifier: Modifier = Modifier
) {
    DetailCard(modifier = modifier, minHeight = 200.dp) {
        CardHeader(iconRes = R.drawable.ic_sunset, title = "Sunset")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text       = sunsetTime,
            color      = WeatherColors.TextPrimary,
            fontSize   = 30.sp,
            fontWeight = FontWeight.Light
        )
        Spacer(modifier = Modifier.height(8.dp))
        SunArc(modifier = Modifier.fillMaxWidth().height(60.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text     = "Sunrise: $sunriseTime",
            color    = WeatherColors.TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SunArc(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Arc path
        val path = Path().apply {
            moveTo(0f, h)
            cubicTo(w * 0.25f, 0f, w * 0.75f, 0f, w, h)
        }
        drawPath(
            path  = path,
            color = Color.White.copy(alpha = 0.3f),
            style = Stroke(
                width = 1.5.dp.toPx(),
                cap   = StrokeCap.Round
            )
        )

        // Sun dot at ~60% through the arc (afternoon position)
        val t    = 0.6f
        val sunX = w * t
        // Rough cubic bezier Y at t=0.6 for control points (0,h) → (w*0.25,0) → (w*0.75,0) → (w,h)
        val sunY = ((1 - t).let { mt ->
            val p0y = h; val p1y = 0f; val p2y = 0f; val p3y = h
            mt * mt * mt * p0y + 3 * mt * mt * t * p1y + 3 * mt * t * t * p2y + t * t * t * p3y
        }).coerceIn(0f, h)

        drawCircle(
            color  = WeatherColors.SunDot,
            radius = 7.dp.toPx(),
            center = Offset(sunX, sunY)
        )
    }
}