package com.example.weather.ui.home.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun UvIndexCard(
    uvIndex: Int,
    label: String,
    description: String,
    modifier: Modifier = Modifier
) {
    DetailCard(modifier = modifier, minHeight = 160.dp) {
        CardHeader(iconRes = R.drawable.ic_uv, title = "UV index")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = WeatherColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = "$uvIndex", color = WeatherColors.TextPrimary, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        UvBar(uvIndex = uvIndex)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, color = WeatherColors.TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun UvBar(uvIndex: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        WeatherColors.UvGreen,
                        WeatherColors.UvYellow,
                        WeatherColors.UvOrange,
                        WeatherColors.UvRed,
                        WeatherColors.UvPurple
                    )
                )
            )
    ) {
        val fraction = uvIndex.coerceIn(0, 11) / 11f
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .wrapContentWidth(Alignment.End)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(WeatherColors.TextPrimary)
                    .align(Alignment.Center)
            )
        }
    }
}