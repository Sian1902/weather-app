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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test — runs on an Android device/emulator.
 * Uses an in-memory Room database so each test starts with a clean slate.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class CityDaoTest {

    private lateinit var db : CityDatabase
    private lateinit var dao: CityDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db  = Room.inMemoryDatabaseBuilder(context, CityDatabase::class.java)
            .allowMainThreadQueries()   // allowed in tests only
            .build()
        dao = db.cityDao()
    }

    @After
    fun tearDown() = db.close()

    // ── Test 1: insertCity and getAllCities ───────────────────────────────────

    @Test
    fun insertCity_and_getAllCities_returnsInsertedCity() = runTest {
        val city = TestFixtures.cityEntity(id = 1, name = "Cairo")
        dao.insertCity(city)

        val cities = dao.getAllCities().first()
        assertEquals(1, cities.size)
        assertEquals("Cairo", cities[0].name)
    }

    // ── Test 2: deleteCity removes it from the list ───────────────────────────

    @Test
    fun deleteCity_removesItFromDatabase() = runTest {
        val city = TestFixtures.cityEntity(id = 2, name = "London")
        dao.insertCity(city)
        dao.deleteCity(city)

        val cities = dao.getAllCities().first()
        assertTrue("City list should be empty after delete", cities.isEmpty())
    }

    // ── Test 3: setDefault clears previous default and sets new one ───────────

    @Test
    fun setDefaultById_clearsOldAndSetsNewDefault() = runTest {
        val cairo  = TestFixtures.cityEntity(id = 1, name = "Cairo",  isDefault = true)
        val london = TestFixtures.cityEntity(id = 2, name = "London", isDefault = false)
        dao.insertCity(cairo)
        dao.insertCity(london)

        // Move default from Cairo (1) to London (2)
        dao.clearAllDefaults()
        dao.setDefaultById(2)

        val defaultCity = dao.getDefaultCity()
        assertNotNull(defaultCity)
        assertEquals("London", defaultCity!!.name)
        assertTrue(defaultCity.isDefault)

        // Cairo must no longer be default
        val allCities = dao.getAllCities().first()
        val cairoRow  = allCities.firstOrNull { it.name == "Cairo" }
        assertNotNull(cairoRow)
        assertFalse("Cairo should no longer be default", cairoRow!!.isDefault)
    }
}