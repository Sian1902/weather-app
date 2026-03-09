package com.example.weather.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun VisibilityCard(
    visibilityKm: Int,
    description: String,
    modifier: Modifier = Modifier
) {
    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(iconRes = R.drawable.ic_visibility, title = "Visibility")
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
            Text(
                text       = "$visibilityKm",
                color      = WeatherColors.TextPrimary,
                fontSize   = 36.sp,
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text     = "km",
                color    = WeatherColors.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Low",  color = WeatherColors.TextSecondary, fontSize = 10.sp)
            Text(text = "High", color = WeatherColors.TextSecondary, fontSize = 10.sp)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(WeatherColors.TextPrimary.copy(alpha = 0.25f))
        ) {
            val fraction = visibilityKm.coerceIn(0, 30) / 30f
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(WeatherColors.TextPrimary)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, color = WeatherColors.TextSecondary, fontSize = 11.sp)
    }
}