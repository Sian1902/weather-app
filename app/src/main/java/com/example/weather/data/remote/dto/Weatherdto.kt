package com.example.weather.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CurrentWeatherDto(
    @SerializedName("coord") val coord: CoordDto,
    @SerializedName("weather") val weather: List<WeatherDescriptionDto>,
    @SerializedName("main") val main: MainDto,
    @SerializedName("visibility") val visibility: Int,
    @SerializedName("wind") val wind: WindDto,
    @SerializedName("clouds") val clouds: CloudsDto,
    @SerializedName("dt") val dt: Long,
    @SerializedName("sys") val sys: SysDto,
    @SerializedName("timezone") val timezone: Int,
    @SerializedName("name") val name: String
)


data class ForecastResponseDto(
    @SerializedName("list") val list: List<ForecastItemDto>,
    @SerializedName("city") val city: CityDto
)

data class ForecastItemDto(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: MainDto,
    @SerializedName("weather") val weather: List<WeatherDescriptionDto>,
    @SerializedName("clouds") val clouds: CloudsDto,
    @SerializedName("wind") val wind: WindDto,
    @SerializedName("visibility") val visibility: Int,
    @SerializedName("pop") val pop: Double,
    @SerializedName("rain") val rain: RainDto?,
    @SerializedName("snow") val snow: SnowDto?,
    @SerializedName("dt_txt") val dtTxt: String
)

data class CoordDto(
    @SerializedName("lon") val lon: Double, @SerializedName("lat") val lat: Double
)

data class WeatherDescriptionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class MainDto(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("sea_level") val seaLevel: Int?,
    @SerializedName("grnd_level") val grndLevel: Int?
)

data class WindDto(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val deg: Int,
    @SerializedName("gust") val gust: Double?
)

data class CloudsDto(
    @SerializedName("all") val all: Int
)

data class SysDto(
    @SerializedName("country") val country: String?,
    @SerializedName("sunrise") val sunrise: Long?,
    @SerializedName("sunset") val sunset: Long?
)

data class CityDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("coord") val coord: CoordDto,
    @SerializedName("country") val country: String,
    @SerializedName("timezone") val timezone: Int,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
)

data class RainDto(
    @SerializedName("3h") val threeHour: Double?
)

data class SnowDto(
    @SerializedName("3h") val threeHour: Double?
)