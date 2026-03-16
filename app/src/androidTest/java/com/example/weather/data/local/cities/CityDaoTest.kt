package com.example.weather.data.local.cities

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weather.TestFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class CityDaoTest {

    private lateinit var db: CityDatabase
    private lateinit var dao: CityDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CityDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.cityDao()
    }

    @After
    fun tearDown() = db.close()


    @Test
    fun insertCity_and_getAllCities_returnsInsertedCity() = runTest {
        val city = TestFixtures.cityEntity(id = 1, name = "Cairo")
        dao.insertCity(city)

        val cities = dao.getAllCities().first()
        assertEquals(1, cities.size)
        assertEquals("Cairo", cities[0].name)
    }


    @Test
    fun deleteCity_removesItFromDatabase() = runTest {
        val city = TestFixtures.cityEntity(id = 2, name = "London")
        dao.insertCity(city)
        dao.deleteCity(city)

        val cities = dao.getAllCities().first()
        assertTrue("City list should be empty after delete", cities.isEmpty())
    }


    @Test
    fun setDefaultById_clearsOldAndSetsNewDefault() = runTest {
        val cairo = TestFixtures.cityEntity(id = 1, name = "Cairo", isDefault = true)
        val london = TestFixtures.cityEntity(id = 2, name = "London", isDefault = false)
        dao.insertCity(cairo)
        dao.insertCity(london)

        dao.clearAllDefaults()
        dao.setDefaultById(2)

        val defaultCity = dao.getDefaultCity()
        assertNotNull(defaultCity)
        assertEquals("London", defaultCity!!.name)
        assertTrue(defaultCity.isDefault)

        val allCities = dao.getAllCities().first()
        val cairoRow = allCities.firstOrNull { it.name == "Cairo" }
        assertNotNull(cairoRow)
        assertFalse("Cairo should no longer be default", cairoRow!!.isDefault)
    }
}