package com.example.weather.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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

@Composable
fun SunCard(
    sunriseTime: String,
    sunsetTime: String,
    modifier: Modifier = Modifier
) {
    DetailCard(modifier = modifier, minHeight = 200.dp) {
        CardHeader(
            iconRes = R.drawable.ic_sunset,
            title = stringResource(R.string.card_sunset)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text       = sunsetTime,
            color      = WeatherColors.TextPrimary,
            fontSize   = 30.sp,
            fontWeight = FontWeight.Light
        )

        Spacer(Modifier.height(16.dp))

        Canvas(modifier = Modifier.fillMaxWidth().height(60.dp)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(0f, h)
                cubicTo(w * 0.3f, 0f, w * 0.7f, 0f, w, h)
            }
            drawPath(path = path, color = Color.White.copy(alpha = 0.2f), style = Stroke(width = 2.dp.toPx()))
        }

        Spacer(Modifier.weight(1f))
        Text(
            text      = stringResource(R.string.sunrise_label, sunriseTime),
            color     = WeatherColors.TextSecondary,
            fontSize  = 12.sp,
            textAlign = if (LocalLayoutDirection.current == LayoutDirection.Rtl)
                TextAlign.End else TextAlign.Start,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}