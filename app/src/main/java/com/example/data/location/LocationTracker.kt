package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class NavigationInfo(
    val distanceMeters: Float,
    val bearingDegrees: Float,
    val relativeHeadingDegrees: Float // Angle of arrow relative to phone's current direction
)

class LocationTracker(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    suspend fun getFreshLocation(): Location? {
        return try {
            val cts = CancellationTokenSource()
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cts.token
            ).await()
            if (location != null) {
                _currentLocation.value = location
            }
            location ?: fusedLocationClient.lastLocation.await()?.also {
                _currentLocation.value = it
            }
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    fun startRealtimeTracking(intervalMillis: Long = 3000L) {
        if (_isTracking.value) return

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
            .setMinUpdateIntervalMillis(1500L)
            .setMinUpdateDistanceMeters(1.0f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    _currentLocation.value = loc
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback!!,
                Looper.getMainLooper()
            )
            _isTracking.value = true
        } catch (e: Exception) {
            _isTracking.value = false
        }
    }

    fun stopRealtimeTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
        _isTracking.value = false
    }

    companion object {
        fun calculateNavigation(
            userLat: Double,
            userLng: Double,
            targetLat: Double,
            targetLng: Double,
            deviceAzimuth: Float?
        ): NavigationInfo {
            val results = FloatArray(2)
            Location.distanceBetween(userLat, userLng, targetLat, targetLng, results)
            val distance = results[0]
            var bearing = results[1] // Initial bearing from user to target in -180..180
            if (bearing < 0) bearing += 360f

            val relativeHeading = if (deviceAzimuth != null) {
                (bearing - deviceAzimuth + 360f) % 360f
            } else {
                bearing
            }

            return NavigationInfo(
                distanceMeters = distance,
                bearingDegrees = bearing,
                relativeHeadingDegrees = relativeHeading
            )
        }
    }
}
