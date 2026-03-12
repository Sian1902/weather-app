package com.example.weather.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun TopBar(
    cityName: String,
    countryCode: String,
    units: String,
    onUnitsToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        IconButton(
            onClick  = onMenuClick,
            modifier = Modifier.size(44.dp).clip(CircleShape)
                .background(WeatherColors.TextPrimary.copy(alpha = 0.2f))
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_menu_add), contentDescription = "Menu",
                tint = WeatherColors.TextPrimary, modifier = Modifier.size(22.dp))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = cityName, color = WeatherColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null,
                    tint = WeatherColors.TextSecondary, modifier = Modifier.size(12.dp))
                Text(text = countryCode, color = WeatherColors.TextSecondary, fontSize = 13.sp)
            }
        }

        Box {
            IconButton(
                onClick  = { menuExpanded = true },
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(WeatherColors.TextPrimary.copy(alpha = 0.2f))
            ) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = WeatherColors.TextPrimary)
            }

            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(
                            if (units == "metric") R.string.menu_switch_to_fahrenheit
                            else                   R.string.menu_switch_to_celsius
                        ))
                    },
                    onClick = { menuExpanded = false; onUnitsToggle() },
                    leadingIcon = {
                        Text(
                            text       = if (units == "metric") "°F" else "°C",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 15.sp,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                Divider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_settings)) },
                    onClick = { menuExpanded = false; onSettingsClick() },
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.ic_menu_add),
                            contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }
        }
    }
}