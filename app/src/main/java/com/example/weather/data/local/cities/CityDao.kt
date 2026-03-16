package com.example.weather.data.local.cities

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {

    @Query("SELECT * FROM cities ORDER BY isDefault DESC, isCurrentLocation DESC, addedAt ASC")
    fun getAllCities(): Flow<List<CityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity)

    @Delete
    suspend fun deleteCity(city: CityEntity)

    @Query("UPDATE cities SET isDefault = 0")
    suspend fun clearAllDefaults()

    @Query("UPDATE cities SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultById(id: Int)

    @Query("SELECT * FROM cities WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultCity(): CityEntity?

    @Query("SELECT * FROM cities WHERE isCurrentLocation = 1 LIMIT 1")
    suspend fun getCurrentLocationCity(): CityEntity?

    @Query(
        """
        UPDATE cities
        SET name = :name, lat = :lat, lon = :lon, addedAt = :addedAt
        WHERE isCurrentLocation = 1
    """
    )
    suspend fun updateCurrentLocationCity(name: String, lat: Double, lon: Double, addedAt: Long)

    @Query("SELECT COUNT(*) FROM cities WHERE isCurrentLocation = 1")
    suspend fun currentLocationExists(): Int
}