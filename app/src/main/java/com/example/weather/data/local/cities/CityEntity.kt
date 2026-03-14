package com.example.weather.data.local.cities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey(autoGenerate = true)
    val id                : Int     = 0,
    val name              : String,
    val lat               : Double,
    val lon               : Double,
    val addedAt           : Long    = System.currentTimeMillis(),
    val isDefault         : Boolean = false,  // only one row has this = true at a time
    val isCurrentLocation : Boolean = false   // the GPS entry — always kept in sync, not deletable
)