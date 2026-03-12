package com.example.weather.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.ui.theme.WeatherColors

@Composable
fun HeroSection(
    currentTemp: String,
    unitSymbol: String,   // "°C" or "°F"
    highTemp: String,
    lowTemp: String
) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Use AnnotatedString so both parts are in ONE Text — immune to RTL reordering
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 96.sp, fontWeight = FontWeight.Thin)) {
                    append(currentTemp)
                }
                withStyle(SpanStyle(fontSize = 36.sp, fontWeight = FontWeight.Thin)) {
                    append(unitSymbol)
                }
            },
            color      = WeatherColors.TextPrimary,
            lineHeight = 100.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text       = "$highTemp/$lowTemp",
            color      = WeatherColors.TextPrimary,
            fontSize   = 18.sp,
            fontWeight = FontWeight.Light
        )
    }
}