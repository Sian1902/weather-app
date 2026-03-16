package com.example.weather.data.local.cities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val lat: Double,
    val lon: Double,
    val addedAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false,
    val isCurrentLocation: Boolean = false
)