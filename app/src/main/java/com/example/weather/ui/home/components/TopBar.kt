package com.example.weather.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun TopBar(
    cityName        : String,
    countryCode     : String,
    units           : String,
    pageCount       : Int    = 1,
    currentPage     : Int    = 0,
    onUnitsToggle   : () -> Unit,
    onSettingsClick : () -> Unit,
    onMenuClick     : () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // ── Left: hamburger / add-city button ────────────────────
        IconButton(
            onClick  = onMenuClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(WeatherColors.TextPrimary.copy(alpha = 0.2f))
        ) {
            Icon(
                painter            = painterResource(id = R.drawable.ic_menu_add),
                contentDescription = stringResource(R.string.cd_menu),
                tint               = WeatherColors.TextPrimary,
                modifier           = Modifier.size(22.dp)
            )
        }

        // ── Centre: city + country + page dots ───────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = cityName,
                color      = WeatherColors.TextPrimary,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint               = WeatherColors.TextSecondary,
                    modifier           = Modifier.size(12.dp)
                )
                Text(text = countryCode, color = WeatherColors.TextSecondary, fontSize = 13.sp)

                // Page indicator dots — only shown when there are multiple cities
                if (pageCount > 1) {
                    Spacer(Modifier.width(4.dp))
                    repeat(pageCount) { index ->
                        val selected = index == currentPage
                        Box(
                            modifier = Modifier
                                .size(if (selected) 7.dp else 4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) WeatherColors.TextPrimary
                                    else WeatherColors.TextPrimary.copy(alpha = 0.35f)
                                )
                        )
                        if (index < pageCount - 1) Spacer(Modifier.width(3.dp))
                    }
                }
            }
        }

        // ── Right: three-dots → glass popup menu ─────────────────
        Box {
            IconButton(
                onClick  = { menuExpanded = true },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(WeatherColors.TextPrimary.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector        = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.cd_more_options),
                    tint               = WeatherColors.TextPrimary
                )
            }

            val density       = LocalDensity.current
            val offsetY       = with(density) { 52.dp.roundToPx() }
            // Capture the localized values from the parent composition — Popup
            // spawns a new window so it does NOT inherit CompositionLocals
            // automatically. We re-provide them so stringResource() inside the
            // popup reads the user's chosen language, not the system locale.
            val configuration   = LocalConfiguration.current
            val layoutDirection = LocalLayoutDirection.current
            // stringResource() resolves strings via LocalContext, not LocalConfiguration.
            // The Popup window loses the localized context, so we must re-wrap it.
            val baseContext     = androidx.compose.ui.platform.LocalContext.current
            val localizedContext = remember(configuration) {
                baseContext.createConfigurationContext(configuration)
            }

            if (menuExpanded) {
                Popup(
                    alignment        = Alignment.TopEnd,
                    offset           = IntOffset(x = 0, y = offsetY),
                    onDismissRequest = { menuExpanded = false },
                    properties       = PopupProperties(focusable = true)
                ) {
                    CompositionLocalProvider(
                        LocalConfiguration                          provides configuration,
                        LocalLayoutDirection                        provides layoutDirection,
                        androidx.compose.ui.platform.LocalContext   provides localizedContext
                    ) {
                        GlassMenuCard(modifier = Modifier.width(220.dp)) {
                            // ── Units row ─────────────────────────
                            MenuRow(
                                icon    = Icons.Default.SettingsSuggest,
                                label   = stringResource(
                                    if (units == "metric") R.string.menu_switch_to_fahrenheit
                                    else                   R.string.menu_switch_to_celsius
                                ),
                                onClick = {
                                    menuExpanded = false
                                    onUnitsToggle()
                                }
                            )

                            HorizontalDivider(
                                color     = WeatherColors.Divider,
                                thickness = 0.5.dp,
                                modifier  = Modifier.padding(horizontal = 8.dp)
                            )

                            // ── Settings row ──────────────────────
                            MenuRow(
                                icon    = Icons.Default.Tune,
                                label   = stringResource(R.string.menu_settings),
                                onClick = {
                                    menuExpanded = false
                                    onSettingsClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Shared helpers ─────────────────────────────────────────────────────────

/**
 * Identical visual style to GlassCard (same corner radius, same background colour)
 * but with a very subtle shadow so it lifts off the cards beneath it.
 */
@Composable
private fun GlassMenuCard(
    modifier  : Modifier = Modifier,
    content   : @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation       = 8.dp,
                shape           = RoundedCornerShape(20.dp),
                ambientColor    = Color.Black.copy(alpha = 0.3f),
                spotColor       = Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(WeatherColors.CardBg),
        content = content
    )
}

@Composable
private fun MenuRow(
    icon    : ImageVector,
    label   : String,
    onClick : () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = WeatherColors.TextPrimary,
            modifier           = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text     = label,
            color    = WeatherColors.TextPrimary,
            fontSize = 15.sp
        )
    }
}