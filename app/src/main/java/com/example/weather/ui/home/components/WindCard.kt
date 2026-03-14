package com.example.weather.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun WindCard(
    windSpeedBft: Int,
    windDeg: Int,
    modifier: Modifier = Modifier
) {
    // GlassCard is the custom container used across the home screen
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Air,
                    contentDescription = null,
                    tint = WeatherColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.label_wind).uppercase(),
                    color = WeatherColors.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = windSpeedBft.toString(),
                        color = WeatherColors.TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.wind_speed_unit),
                        color = WeatherColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Wind Direction Arrow
                Icon(
                    imageVector = Icons.Default.Air, // You can replace this with a dedicated arrow icon
                    contentDescription = stringResource(R.string.desc_wind_direction),
                    tint = WeatherColors.TextPrimary,
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(windDeg.toFloat()) // Rotates the icon to show direction
                )
            }
        }
    }
}