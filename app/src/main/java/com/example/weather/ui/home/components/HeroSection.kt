package com.example.weather.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weather.R
import com.example.weather.ui.theme.WeatherColors

@Composable
fun HeroSection(
    temp: String, unit: String, high: String, low: String, description: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 96.sp, fontWeight = FontWeight.Thin)) {
                    append(temp)
                }
                withStyle(SpanStyle(fontSize = 36.sp, fontWeight = FontWeight.Thin)) {
                    append(unit)
                }
            }, color = WeatherColors.TextPrimary, lineHeight = 100.sp
        )

        Text(
            text = description,
            color = WeatherColors.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.temp_high_low, high, low),
            color = WeatherColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Light
        )
    }
}