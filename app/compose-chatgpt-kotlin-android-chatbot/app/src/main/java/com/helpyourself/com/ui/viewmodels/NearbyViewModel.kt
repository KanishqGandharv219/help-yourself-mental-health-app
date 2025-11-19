package com.helpyourself.com.ui.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.compose.runtime.*
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.helpyourself.com.R
import com.helpyourself.com.models.PlaceData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.cos
import com.helpyourself.com.utils.awaitHighAccuracyFix
import android.util.Log

@HiltViewModel
class NearbyViewModel @Inject constructor(): ViewModel() {
    private lateinit var placesClient: PlacesClient
    private lateinit var appContext: Context

    var userLocation by mutableStateOf<LatLng?>(null)
    var places by mutableStateOf<List<PlaceData>>(emptyList())
    var errorMessage by mutableStateOf<String?>(null)

    fun initialize(context: Context) {
        appContext = context.applicationContext
        if (!Places.isInitialized()) {
            Places.initialize(appContext, appContext.getString(R.string.google_maps_api_key))
        }
        placesClient = Places.createClient(appContext)
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchLocationAndPlaces(locationClient: FusedLocationProviderClient): LatLng? {
        errorMessage = null
        places = emptyList()

        val locMgr = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!LocationManagerCompat.isLocationEnabled(locMgr)) {
            errorMessage = "Please enable location services"
            return null
        }

        // Request a fresh high-accuracy location fix with a 10-second timeout
        val androidLoc = locationClient.awaitHighAccuracyFix(10_000)
        if (androidLoc == null) {
            errorMessage = "Couldn't get your position – check GPS permissions/settings"
            return null
        }

        val loc = LatLng(androidLoc.latitude, androidLoc.longitude)
        userLocation = loc

        // 6) Build 5 km bounding box (still used for fallback searchNearby)
        val latRad = Math.toRadians(loc.latitude)
        val degLatKm = 110.574              // km per degree latitude
        val degLongKm = 110.572 * cos(latRad)  // km per degree longitude
        val deltaLat = 5.0 / degLatKm          // use 5.0 (Double)
        val deltaLong = 5.0 / degLongKm        // use 5.0 (Double)
        val bounds = RectangularBounds.newInstance(
            LatLng(loc.latitude - deltaLat, loc.longitude - deltaLong),
            LatLng(loc.latitude + deltaLat, loc.longitude + deltaLong)
        )

        // 7) Try text-based search first (therapist keyword)
        places = try {
            searchTherapistsByText(loc)
        } catch (e: Exception) {
            emptyList()
        }

        // 8) Fallback to generic health type if text search found nothing
        if (places.isEmpty()) {
            places = try {
                searchNearbyPlaces(loc)
            } catch (_: Exception) { emptyList() }
        }

        if (places.isEmpty()) {
            errorMessage = "No therapists found nearby."
        }

        return loc
    }

    /** Text search for keyword "therapist" within 5 km circle */
    private suspend fun searchTherapistsByText(loc: LatLng): List<PlaceData> {
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.RATING
        )

        val circle = CircularBounds.newInstance(loc, 5000.0)

        val request = SearchByTextRequest.builder("therapist", fields)
            // Bias results to a 5-km circle around the user
            .setLocationBias(circle)
            .setMaxResultCount(20)
            .setRankPreference(SearchByTextRequest.RankPreference.RELEVANCE)
            .build()

        Log.d("NearbyVM", "Text-searching therapist within 5km…")

        val response = placesClient.searchByText(request).await()
        Log.d("NearbyVM", "Text search returned ${response.places.size} results")

        return response.places.map { p ->
            PlaceData(
                id = p.id.orEmpty(),
                name = p.name.orEmpty(),
                address = p.formattedAddress.orEmpty(),
                latLng = p.latLng ?: loc,
                rating = p.rating ?: 0.0
            )
        }.sortedByDescending { it.rating }
    }

    @SuppressLint("MissingPermission")
    private suspend fun searchNearbyPlaces(loc: LatLng): List<PlaceData> {
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.RATING
        )

        // Build a 5-km bounding box around the user location
        val latRad = Math.toRadians(loc.latitude)
        val degLatKm = 110.574
        val degLngKm = 110.572 * cos(latRad)
        val deltaLat = 5.0 / degLatKm
        val deltaLng = 5.0 / degLngKm

        val bounds = RectangularBounds.newInstance(
            LatLng(loc.latitude - deltaLat, loc.longitude - deltaLng),
            LatLng(loc.latitude + deltaLat, loc.longitude + deltaLng)
        )

        val request = SearchNearbyRequest.builder(bounds, fields)
            .setIncludedTypes(listOf("health"))
            .setMaxResultCount(20)
            .build()

        Log.d("NearbyVM", "Fallback type search (health) near $loc …")

        val response = placesClient.searchNearby(request).await()
        Log.d("NearbyVM", "Places returned ${response.places.size} results")

        return response.places.map { p ->
            PlaceData(
                id = p.id.orEmpty(),
                name = p.name.orEmpty(),
                address = p.formattedAddress.orEmpty(),
                latLng = p.latLng ?: loc,
                rating = p.rating ?: 0.0
            )
        }.sortedByDescending { it.rating }
    }
}
