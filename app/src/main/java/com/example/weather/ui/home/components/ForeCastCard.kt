package com.example.weather.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weather.R
import com.example.weather.ui.home.DailyItem
import com.example.weather.ui.theme.WeatherColors


@Composable
fun ForecastCard(
    items: List<DailyItem>,
    onExtendedForecastClick: () -> Unit = {}
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = null,
                        tint = WeatherColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Weather forecast",
                        color = WeatherColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_list_view),
                        contentDescription = "List view",
                        tint = WeatherColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chart_view),
                        contentDescription = "Chart view",
                        tint = WeatherColors.TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items.forEach { item ->
                    DailyItemView(
                        item     = item,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            SparkLine(
                values = items.map { it.high.removeSuffix("°").toFloat() },
                color  = WeatherColors.SparkHigh
            )

            Spacer(modifier = Modifier.height(4.dp))
            SparkLine(
                values = items.map { it.low.removeSuffix("°").toFloat() },
                color  = WeatherColors.SparkLow
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items.forEach { item ->
                    Text(
                        text      = item.low,
                        color     = WeatherColors.TextSecondary,
                        fontSize  = 13.sp,
                        modifier  = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(WeatherColors.TextPrimary.copy(alpha = 0.15f))
                    .clickable { onExtendedForecastClick() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text     = "Extended forecast",
                    color    = WeatherColors.TextPrimary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun DailyItemView(item: DailyItem, modifier: Modifier = Modifier) {
    Column(
        modifier             = modifier,
        horizontalAlignment  = Alignment.CenterHorizontally,
        verticalArrangement  = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text       = item.day,
            color      = WeatherColors.TextPrimary,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text     = item.date,
            color    = WeatherColors.TextSecondary,
            fontSize = 11.sp
        )
        AsyncImage(
            model             = "https://openweathermap.org/img/wn/${item.iconCode}@2x.png",
            contentDescription = null,
            modifier           = Modifier.size(32.dp)
        )
        Text(
            text       = item.high,
            color      = WeatherColors.TextPrimary,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}