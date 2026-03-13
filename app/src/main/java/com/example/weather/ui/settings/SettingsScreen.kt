package com.example.weather.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun SettingsScreen(
    viewModel       : SettingsViewModel,
    onBack          : () -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val prefs                     by viewModel.preferences.collectAsState()
    val needsExactNotif           by viewModel.needsExactAlarmPermission.collectAsState()
    val needsExactAlarm           by viewModel.needsExactAlarmPermissionAlarm.collectAsState()
    var showNotifTimePicker        by remember { mutableStateOf(false) }
    var showAlarmTimePicker        by remember { mutableStateOf(false) }
    val context                    = LocalContext.current
    val lifecycleOwner             = LocalLifecycleOwner.current

    // Re-check exact alarm permission when returning from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.recheckExactAlarmPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // POST_NOTIFICATIONS runtime permission launcher (Android 13+)
    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.toggleNotifications() }

    val alarmPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.toggleAlarm() }

    fun onNotificationToggle() {
        if (!prefs.notificationsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val has = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!has) { notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS); return }
        }
        viewModel.toggleNotifications()
    }

    fun onAlarmToggle() {
        if (!prefs.alarmEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val has = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!has) { alarmPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS); return }
        }
        viewModel.toggleAlarm()
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
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                        tint = WeatherColors.TextPrimary)
                }
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.settings_title), color = WeatherColors.TextPrimary,
                    fontSize = 20.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(8.dp))

            // ── Language ──────────────────────────────────────────────────────
            SectionLabel(stringResource(R.string.settings_language))
            Spacer(Modifier.height(8.dp))
            SegmentedControl(
                options  = listOf("en" to stringResource(R.string.language_english),
                    "ar" to stringResource(R.string.language_arabic)),
                selected = prefs.language,
                onSelect = { lang -> viewModel.setLanguage(lang); onLanguageChange(lang) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(28.dp))

            // ── Temperature unit ──────────────────────────────────────────────
            SectionLabel(stringResource(R.string.settings_units))
            Spacer(Modifier.height(8.dp))
            SegmentedControl(
                options  = listOf("metric"   to stringResource(R.string.units_celsius),
                    "imperial" to stringResource(R.string.units_fahrenheit)),
                selected = prefs.units,
                onSelect = { chosen -> if (chosen != prefs.units) viewModel.toggleUnits() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(28.dp))

            // ── Daily Notification ────────────────────────────────────────────
            SectionLabel(stringResource(R.string.settings_notifications))
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                ToggleRow(
                    title       = stringResource(R.string.settings_notif_daily),
                    description = stringResource(R.string.settings_notif_daily_desc),
                    checked     = prefs.notificationsEnabled,
                    onToggle    = { onNotificationToggle() }
                )
                if (prefs.notificationsEnabled && needsExactNotif) {
                    PermissionBannerRow(onGrant = { viewModel.openExactAlarmSettings() })
                }
                if (prefs.notificationsEnabled) {
                    SettingsDivider()
                    TimeRow(
                        title       = stringResource(R.string.settings_notif_time),
                        description = stringResource(R.string.settings_notif_time_desc),
                        hour        = prefs.notificationHour,
                        minute      = prefs.notificationMinute,
                        onClick     = { showNotifTimePicker = true }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Daily Alarm ───────────────────────────────────────────────────
            SectionLabel(stringResource(R.string.settings_alarm))
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                ToggleRow(
                    title       = stringResource(R.string.settings_alarm_daily),
                    description = stringResource(R.string.settings_alarm_daily_desc),
                    checked     = prefs.alarmEnabled,
                    onToggle    = { onAlarmToggle() }
                )
                if (prefs.alarmEnabled && needsExactAlarm) {
                    PermissionBannerRow(onGrant = { viewModel.openExactAlarmSettings() })
                }
                if (prefs.alarmEnabled) {
                    SettingsDivider()
                    TimeRow(
                        title       = stringResource(R.string.settings_alarm_time),
                        description = stringResource(R.string.settings_alarm_time_desc),
                        hour        = prefs.alarmHour,
                        minute      = prefs.alarmMinute,
                        onClick     = { showAlarmTimePicker = true }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // ── Time picker dialogs ───────────────────────────────────────────────────
    if (showNotifTimePicker) {
        WeatherTimePicker(
            title         = stringResource(R.string.settings_notif_pick_time),
            initialHour   = prefs.notificationHour,
            initialMinute = prefs.notificationMinute,
            onConfirm     = { h, m -> viewModel.setNotificationTime(h, m); showNotifTimePicker = false },
            onDismiss     = { showNotifTimePicker = false }
        )
    }
    if (showAlarmTimePicker) {
        WeatherTimePicker(
            title         = stringResource(R.string.settings_alarm_pick_time),
            initialHour   = prefs.alarmHour,
            initialMinute = prefs.alarmMinute,
            onConfirm     = { h, m -> viewModel.setAlarmTime(h, m); showAlarmTimePicker = false },
            onDismiss     = { showAlarmTimePicker = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, color = WeatherColors.TextSecondary, fontSize = 11.sp,
        fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp,
        modifier = Modifier.padding(horizontal = 20.dp))
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(WeatherColors.CardBgDark),
        content = content
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = WeatherColors.Divider, thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 20.dp))
}

/** Toggle row — enable / disable switch with title + description. */
@Composable
private fun ToggleRow(
    title      : String,
    description: String,
    checked    : Boolean,
    onToggle   : () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, color = WeatherColors.TextPrimary, fontSize = 15.sp,
                fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(description, color = WeatherColors.TextSecondary, fontSize = 12.sp,
                lineHeight = 16.sp)
        }
        Switch(
            checked         = checked,
            onCheckedChange = { onToggle() },
            colors          = SwitchDefaults.colors(
                checkedThumbColor       = WeatherColors.TextPrimary,
                checkedTrackColor       = WeatherColors.TextPrimary.copy(alpha = 0.35f),
                uncheckedThumbColor     = WeatherColors.TextSecondary,
                uncheckedTrackColor     = WeatherColors.TextSecondary.copy(alpha = 0.2f),
                uncheckedBorderColor    = Color.Transparent,
                checkedBorderColor      = Color.Transparent
            )
        )
    }
}

/** Amber banner shown when exact alarm permission is not granted on Android 12+. */
@Composable
private fun PermissionBannerRow(onGrant: () -> Unit) {
    SettingsDivider()
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFB300).copy(alpha = 0.15f))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Default.Warning, contentDescription = null,
            tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.notif_exact_alarm_title),
                color = WeatherColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(stringResource(R.string.notif_exact_alarm_desc),
                color = WeatherColors.TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
        }
        TextButton(onClick = onGrant) {
            Text(stringResource(R.string.notif_exact_alarm_grant),
                color = Color(0xFFFFB300), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Tappable row that shows the current time and opens the time picker. */
@Composable
private fun TimeRow(
    title      : String,
    description: String,
    hour       : Int,
    minute     : Int,
    onClick    : () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, color = WeatherColors.TextPrimary, fontSize = 15.sp,
                fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(description, color = WeatherColors.TextSecondary, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(WeatherColors.TextPrimary.copy(alpha = 0.2f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("%02d:%02d".format(hour, minute), color = WeatherColors.TextPrimary,
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SegmentedControl(
    options  : List<Pair<String, String>>,
    selected : String,
    onSelect : (String) -> Unit,
    modifier : Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(WeatherColors.CardBgDark)
            .border(1.dp, WeatherColors.Divider, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { (value, label) ->
            val active = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) WeatherColors.TextPrimary.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label,
                    color      = if (active) WeatherColors.TextPrimary else WeatherColors.TextSecondary,
                    fontSize   = 15.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherTimePicker(
    title        : String,
    initialHour  : Int,
    initialMinute: Int,
    onConfirm    : (hour: Int, minute: Int) -> Unit,
    onDismiss    : () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour   = initialHour,
        initialMinute = initialMinute,
        is24Hour      = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = WeatherColors.CardBgDark,
        tonalElevation   = 0.dp,
        shape            = RoundedCornerShape(20.dp),
        title = {
            Text(title, color = WeatherColors.TextPrimary, fontSize = 16.sp,
                fontWeight = FontWeight.Medium)
        },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(
                    state  = state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor                      = WeatherColors.CardBg,
                        clockDialSelectedContentColor       = WeatherColors.TextPrimary,
                        clockDialUnselectedContentColor     = WeatherColors.TextSecondary,
                        selectorColor                       = WeatherColors.TextPrimary.copy(alpha = 0.7f),
                        containerColor                      = WeatherColors.CardBgDark,
                        periodSelectorBorderColor           = WeatherColors.Divider,
                        timeSelectorSelectedContainerColor  = WeatherColors.TextPrimary.copy(alpha = 0.2f),
                        timeSelectorUnselectedContainerColor= WeatherColors.CardBg,
                        timeSelectorSelectedContentColor    = WeatherColors.TextPrimary,
                        timeSelectorUnselectedContentColor  = WeatherColors.TextSecondary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(R.string.settings_ok), color = WeatherColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_cancel), color = WeatherColors.TextSecondary)
            }
        }
    )
}