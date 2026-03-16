package com.example.weather.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weather.R
import com.example.weather.ui.home.HourlyItem
import com.example.weather.ui.theme.WeatherColors

@Composable
fun HourlyWeatherCard(items: List<HourlyItem>) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bell),
                    contentDescription = null,
                    tint = WeatherColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.hourly_weather),
                    color = WeatherColors.TextSecondary,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = WeatherColors.Divider, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                items(items) { item ->
                    HourlyItemView(item)
                }
            }
        }
    }
}

@Composable
private fun HourlyItemView(item: HourlyItem) {
    val label = if (item.label == "NOW") stringResource(R.string.label_now) else item.label
    val isNow = item.label == "NOW"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = if (isNow) WeatherColors.TextPrimary else WeatherColors.TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal
        )
        AsyncImage(
            model = "https://openweathermap.org/img/wn/${item.iconCode}@2x.png",
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = item.temp,
            color = WeatherColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}