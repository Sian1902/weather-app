package com.example.weather.data.remote

import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherApiService {


    @GET("weather")
    suspend fun getCurrentWeatherByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): CurrentWeatherDto

    @GET("weather")
    suspend fun getCurrentWeatherByCityName(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): CurrentWeatherDto

    @GET("forecast")
    suspend fun getForecastByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en",
        @Query("cnt") count: Int? = null
    ): ForecastResponseDto

    @GET("forecast")
    suspend fun getForecastByCityName(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en",
        @Query("cnt") count: Int? = null
    ): ForecastResponseDto
}