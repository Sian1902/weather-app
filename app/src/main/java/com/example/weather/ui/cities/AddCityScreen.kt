package com.example.weather.ui.cities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceManager
import com.example.weather.R
import com.example.weather.data.local.cities.CityEntity
import com.example.weather.ui.theme.WeatherColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCityScreen(
    onBack: () -> Unit, onConfirm: (CityEntity) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf(emptyList<PlaceSuggestion>()) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }

    remember(context) {
        Configuration.getInstance().apply {
            load(context, PreferenceManager.getDefaultSharedPreferences(context))
            userAgentValue = context.packageName
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            isSearching = true
            delay(400)
            suggestions = fetchPlaceSuggestions(searchQuery)
            isSearching = false
        } else {
            suggestions = emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        WeatherColors.SkyTop,
                        WeatherColors.SkyMid,
                        WeatherColors.SkyDeep,
                        WeatherColors.SkyBottom
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack, contentDescription = null,
                        tint = WeatherColors.TextPrimary
                    )
                }
                Text(
                    stringResource(R.string.add_city_title),
                    color = WeatherColors.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { isSearchActive = false; keyboardController?.hide() },
                active = isSearchActive,
                onActiveChange = { isSearchActive = it },
                placeholder = {
                    Text(
                        stringResource(R.string.add_city_search_hint),
                        color = WeatherColors.TextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.manage_cities_add),
                        tint = WeatherColors.TextSecondary
                    )
                },
                trailingIcon = {
                    if (isSearching) CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = WeatherColors.TextSecondary
                    )
                },
                colors = SearchBarDefaults.colors(
                    containerColor = WeatherColors.CardBgDark,
                    inputFieldColors = TextFieldDefaults.colors(
                        focusedTextColor = WeatherColors.TextPrimary,
                        unfocusedTextColor = WeatherColors.TextPrimary,
                        cursorColor = WeatherColors.TextPrimary
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isSearchActive) 0.dp else 16.dp)
            ) {
                LazyColumn {
                    items(suggestions) { suggestion ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    suggestion.displayName,
                                    color = WeatherColors.TextPrimary,
                                    fontSize = 14.sp
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = WeatherColors.CardBgDark),
                            modifier = Modifier.clickable {
                                searchQuery = suggestion.displayName
                                selectedName = suggestion.displayName
                                selectedPoint = GeoPoint(suggestion.lat, suggestion.lon)
                                isSearchActive = false
                                keyboardController?.hide()
                            })
                        HorizontalDivider(color = WeatherColors.Divider, thickness = 0.5.dp)
                    }
                }
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(5.0)
                        controller.setCenter(GeoPoint(30.0, 31.0))

                        val tapReceiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                selectedPoint = p
                                selectedName = null
                                isSearchActive = false
                                keyboardController?.hide()
                                return true
                            }

                            override fun longPressHelper(p: GeoPoint) = false
                        }
                        overlays.add(MapEventsOverlay(tapReceiver))
                    }
                },
                update = { mapView ->
                    mapView.overlays.removeAll { it is Marker }
                    selectedPoint?.let { point ->
                        Marker(mapView).apply {
                            position = point
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = selectedName ?: String.format(
                                Locale.getDefault(),
                                "%.4f, %.4f",
                                point.latitude,
                                point.longitude
                            )
                            mapView.overlays.add(this)
                            mapView.controller.animateTo(point)
                        }
                    }
                    mapView.invalidate()
                })

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(WeatherColors.CardBgDark)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                if (selectedPoint != null) {
                    Text(
                        text = selectedName ?: String.format(
                            Locale.getDefault(),
                            "%.4f°, %.4f°",
                            selectedPoint!!.latitude,
                            selectedPoint!!.longitude
                        ),
                        color = WeatherColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%.6f, %.6f",
                            selectedPoint!!.latitude,
                            selectedPoint!!.longitude
                        ), color = WeatherColors.TextSecondary, fontSize = 12.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val pt = selectedPoint ?: return@Button
                            val name = selectedName ?: String.format(
                                Locale.getDefault(),
                                "%.4f, %.4f",
                                pt.latitude,
                                pt.longitude
                            )
                            onConfirm(
                                CityEntity(
                                    name = name,
                                    lat = pt.latitude,
                                    lon = pt.longitude
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WeatherColors.TextPrimary.copy(alpha = 0.25f),
                            contentColor = WeatherColors.TextPrimary
                        )
                    ) {
                        Text(
                            stringResource(R.string.add_city_confirm),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        stringResource(R.string.add_city_hint),
                        color = WeatherColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

data class PlaceSuggestion(val displayName: String, val lat: Double, val lon: Double)

suspend fun fetchPlaceSuggestions(query: String): List<PlaceSuggestion> =
    withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val results = mutableListOf<PlaceSuggestion>()
        runCatching {
            val url = URL("https://photon.komoot.io/api/?q=${query.replace(" ", "+")}&limit=6")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val features = json.getJSONArray("features")
                for (i in 0 until features.length()) {
                    val feat = features.getJSONObject(i)
                    val coords = feat.getJSONObject("geometry").getJSONArray("coordinates")
                    val lon = coords.getDouble(0)
                    val lat = coords.getDouble(1)
                    val props = feat.getJSONObject("properties")
                    val name = listOf(
                        props.optString("name", ""),
                        props.optString("city", ""),
                        props.optString("country", "")
                    ).filter { it.isNotBlank() }.joinToString(", ")
                    if (name.isNotBlank()) results.add(PlaceSuggestion(name, lat, lon))
                }
            }
        }
        results
    }