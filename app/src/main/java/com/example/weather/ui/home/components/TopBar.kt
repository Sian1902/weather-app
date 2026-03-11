package com.example.weather.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun TopBar(
    cityName: String,
    countryCode: String,
    onMenuClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(WeatherColors.TextPrimary.copy(alpha = 0.2f))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menu_add),
                contentDescription = "Menu",
                tint = WeatherColors.TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // City name
            Text(
                text       = cityName,
                color      = WeatherColors.TextPrimary,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Medium
            )
            // Country code beneath city name
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector   = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint          = WeatherColors.TextSecondary,
                    modifier      = Modifier.size(12.dp)
                )
                Text(
                    text     = countryCode,
                    color    = WeatherColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        IconButton(
            onClick = onMoreClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(WeatherColors.TextPrimary.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector   = Icons.Default.MoreVert,
                contentDescription = "More",
                tint          = WeatherColors.TextPrimary
            )
        }
    }
}