// ── SunCard.kt ───────────────────────────────────────────────────────────────
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
import androidx.compose.ui.res.stringResource
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
        CardHeader(iconRes = R.drawable.ic_sunset, title = stringResource(R.string.card_sunset))
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
            text     = stringResource(R.string.sunrise_label, sunriseTime),
            color    = WeatherColors.TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SunArc(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val path = Path().apply { moveTo(0f, h); cubicTo(w * 0.25f, 0f, w * 0.75f, 0f, w, h) }
        drawPath(path = path, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
        val t = 0.6f; val sunX = w * t
        val sunY = ((1 - t).let { mt ->
            mt*mt*mt*h + 3*mt*mt*t*0f + 3*mt*t*t*0f + t*t*t*h
        }).coerceIn(0f, h)
        drawCircle(color = WeatherColors.SunDot, radius = 7.dp.toPx(), center = Offset(sunX, sunY))
    }
}