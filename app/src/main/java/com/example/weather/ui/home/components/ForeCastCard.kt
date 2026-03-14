package com.example.weather.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
                // Actions (Icons) - Arrangement.SpaceBetween handles RTL flip
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chart_view),
                        contentDescription = null,
                        tint = WeatherColors.TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_list_view),
                        contentDescription = null,
                        tint = WeatherColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Title and Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.weather_forecast),
                        color = WeatherColors.TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = null,
                        tint = WeatherColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items.forEach { item ->
                    DailyItemView(item = item, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Ensure numeric filtering works for localized digits if necessary
            SparkLine(
                values = items.map { it.high.replace(Regex("[^0-9-]"), "").toFloatOrNull() ?: 0f },
                color  = WeatherColors.SparkHigh
            )
            Spacer(modifier = Modifier.height(4.dp))
            SparkLine(
                values = items.map { it.low.replace(Regex("[^0-9-]"), "").toFloatOrNull() ?: 0f },
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
        }
    }
}

@Composable
private fun DailyItemView(item: DailyItem, modifier: Modifier = Modifier) {
    val dayLabel = when (item.day.uppercase()) {
        "TODAY" -> stringResource(R.string.day_today)
        "MON"   -> stringResource(R.string.day_monday)
        "TUE"   -> stringResource(R.string.day_tuesday)
        "WED"   -> stringResource(R.string.day_wednesday)
        "THU"   -> stringResource(R.string.day_thursday)
        "FRI"   -> stringResource(R.string.day_friday)
        "SAT"   -> stringResource(R.string.day_saturday)
        "SUN"   -> stringResource(R.string.day_sunday)
        else    -> item.day
    }

    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text       = dayLabel,
            color      = WeatherColors.TextPrimary,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines   = 1
        )
        Text(
            text     = item.date,
            color    = WeatherColors.TextSecondary,
            fontSize = 11.sp,
            maxLines = 1
        )
        AsyncImage(
            model              = "https://openweathermap.org/img/wn/${item.iconCode}@2x.png",
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