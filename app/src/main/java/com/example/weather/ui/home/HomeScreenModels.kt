package com.example.weather.ui.home

data class HourlyItem(
    val label: String,
    val iconCode: String,
    val temp: String
)

data class DailyItem(
    val day: String,
    val date: String,
    val iconCode: String,
    val high: String,
    val low: String
)

fun sampleHourly() = listOf(
    HourlyItem("Now",   "02d", "14°"),
    HourlyItem("10:00", "01d", "17°"),
    HourlyItem("11:00", "01d", "19°"),
    HourlyItem("12:00", "01d", "20°"),
    HourlyItem("13:00", "01d", "21°"),
    HourlyItem("14:00", "01d", "22°"),
    HourlyItem("15:00", "02d", "21°"),
)

fun sampleDaily() = listOf(
    DailyItem("Today", "3/7",  "02d", "23°", "13°"),
    DailyItem("Sun",   "3/8",  "02d", "21°", "13°"),
    DailyItem("Mon",   "3/9",  "03d", "20°", "12°"),
    DailyItem("Tue",   "3/10", "02d", "22°", "12°"),
    DailyItem("Wed",   "3/11", "03d", "24°", "13°"),
)