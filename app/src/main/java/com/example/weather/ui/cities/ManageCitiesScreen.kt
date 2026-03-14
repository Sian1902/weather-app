package com.example.weather.ui.cities

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.data.local.cities.CityEntity
import com.example.weather.ui.theme.WeatherColors

@Composable
fun ManageCitiesScreen(
    viewModel      : CitiesViewModel,
    onBack         : () -> Unit,
    onOpenCity     : (CityEntity) -> Unit,
    onSetDefault   : (CityEntity) -> Unit
) {
    val items      by viewModel.items.collectAsState()
    var showAddMap  by remember { mutableStateOf(false) }

    if (showAddMap) {
        AddCityScreen(
            onBack    = { showAddMap = false },
            onConfirm = { city ->
                viewModel.addCity(city)
                showAddMap = false
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(WeatherColors.SkyTop, WeatherColors.SkyMid,
                        WeatherColors.SkyDeep, WeatherColors.SkyBottom)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Fixed RTL spacing
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.settings_back),
                        tint = WeatherColors.TextPrimary
                    )
                }
                Text(
                    stringResource(R.string.manage_cities_title),
                    color      = WeatherColors.TextPrimary,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier.weight(1f)
                )
            }

            // ── City list ─────────────────────────────────────────────────────
            LazyColumn(
                modifier            = Modifier.weight(1f),
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.entity.id }) { item ->
                    CityCard(
                        item         = item,
                        onOpen       = { onOpenCity(item.entity) },
                        onSetDefault = {
                            viewModel.setDefault(item.entity)
                            onSetDefault(item.entity)
                        },
                        onDelete     = {
                            if (!item.entity.isCurrentLocation)
                                viewModel.deleteCity(item.entity)
                        }
                    )
                }

                if (items.isEmpty()) {
                    item {
                        Box(
                            modifier         = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.manage_cities_empty),
                                color    = WeatherColors.TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // ── Add city button ───────────────────────────────────────────────
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier            = Modifier.clickable { showAddMap = true },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(WeatherColors.TextPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.manage_cities_add), // Fixed hardcoded
                            tint     = WeatherColors.TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.manage_cities_add),
                        color    = WeatherColors.TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CityCard(
    item        : CityUiItem,
    onOpen      : () -> Unit,
    onSetDefault: () -> Unit,
    onDelete    : () -> Unit
) {
    val isDefault         = item.entity.isDefault
    val isCurrentLocation = item.entity.isCurrentLocation

    val borderModifier = if (isDefault)
        Modifier.clip(RoundedCornerShape(20.dp))
            .background(WeatherColors.TextPrimary.copy(alpha = 0.08f))
    else
        Modifier.clip(RoundedCornerShape(20.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier)
            .background(WeatherColors.CardBgDark)
            .clickable(onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isCurrentLocation) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint     = WeatherColors.TextPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
        }

        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text       = item.entity.name,
                color      = WeatherColors.TextPrimary,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            if (item.loading) {
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    modifier   = Modifier
                        .width(80.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color      = WeatherColors.TextSecondary.copy(alpha = 0.5f),
                    trackColor = Color.Transparent
                )
            } else {
                item.snapshot?.let { snap ->
                    Spacer(Modifier.height(2.dp))
                    Text(snap.desc, color = WeatherColors.TextSecondary, fontSize = 13.sp)
                }
                if (isDefault) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(R.string.city_default_label),
                        color    = WeatherColors.TextPrimary.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (!item.loading) {
            item.snapshot?.let { snap ->
                Text(
                    "${snap.temp}${snap.unitSymbol}",
                    color      = WeatherColors.TextPrimary,
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }

        Spacer(Modifier.width(4.dp))

        IconButton(
            onClick  = onSetDefault,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector        = if (isDefault) Icons.Filled.Star
                else           Icons.Outlined.StarOutline,
                contentDescription = if (isDefault) stringResource(R.string.city_default_label)
                else stringResource(R.string.city_set_default),
                tint     = if (isDefault) Color(0xFFFFD700)
                else           WeatherColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        if (!isCurrentLocation) {
            IconButton(
                onClick  = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.city_delete),
                    tint     = WeatherColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Spacer(Modifier.size(36.dp))
        }
    }
}