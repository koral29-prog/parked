package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.location.Location
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppThemeMode
import com.example.data.local.AppLanguage
import com.example.data.local.DistanceUnit
import com.example.data.local.ParkingEntity
import com.example.data.local.ParkingRepository
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.location.LocationTracker
import com.example.data.location.NavigationInfo
import com.example.data.sensor.CompassManager
import com.example.util.HapticHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ParkingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val parkingRepository = ParkingRepository(db.parkingDao())
    private val preferencesRepository = UserPreferencesRepository(application)
    private val locationTracker = LocationTracker(application)
    private val compassManager = CompassManager(application)

    val activeParking: StateFlow<ParkingEntity?> = parkingRepository.activeParking
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allHistory: StateFlow<List<ParkingEntity>> = parkingRepository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences

    val currentLocation: StateFlow<Location?> = locationTracker.currentLocation
    val compassHeading: StateFlow<Float?> = compassManager.azimuthDegrees
    val isCompassAvailable: StateFlow<Boolean> = compassManager.isSensorAvailable

    private val _isAcquiringLocation = MutableStateFlow(false)
    val isAcquiringLocation: StateFlow<Boolean> = _isAcquiringLocation.asStateFlow()

    private val _lastCapturedLocation = MutableStateFlow<Location?>(null)
    val lastCapturedLocation: StateFlow<Location?> = _lastCapturedLocation.asStateFlow()

    // Real-time navigation info calculated from current user location & parked location
    val navigationInfo: StateFlow<NavigationInfo?> = combine(
        activeParking,
        currentLocation,
        compassHeading
    ) { active, currentLoc, heading ->
        if (active != null && active.latitude != null && active.longitude != null && currentLoc != null) {
            LocationTracker.calculateNavigation(
                userLat = currentLoc.latitude,
                userLng = currentLoc.longitude,
                targetLat = active.latitude,
                targetLng = active.longitude,
                deviceAzimuth = heading
            )
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Start sensor and live tracker when needed
        compassManager.startListening()
    }

    fun startRealtimeTracking() {
        locationTracker.startRealtimeTracking()
        compassManager.startListening()
    }

    fun stopRealtimeTracking() {
        locationTracker.stopRealtimeTracking()
    }

    fun acquireQuickLocation(onComplete: (Location?) -> Unit = {}) {
        viewModelScope.launch {
            _isAcquiringLocation.value = true
            val loc = locationTracker.getFreshLocation()
            _lastCapturedLocation.value = loc
            _isAcquiringLocation.value = false
            onComplete(loc)
        }
    }

    fun saveParkingSession(
        latitude: Double?,
        longitude: Double?,
        accuracyMeters: Float?,
        floor: String?,
        section: String?,
        spotNumber: String?,
        note: String?,
        photoUri: String?,
        meterMinutes: Int? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val expiry = if (meterMinutes != null && meterMinutes > 0) {
                System.currentTimeMillis() + (meterMinutes * 60 * 1000L)
            } else null

            val entity = ParkingEntity(
                latitude = latitude,
                longitude = longitude,
                accuracyMeters = accuracyMeters,
                timestamp = System.currentTimeMillis(),
                floor = floor?.trim()?.ifEmpty { null },
                section = section?.trim()?.ifEmpty { null },
                spotNumber = spotNumber?.trim()?.ifEmpty { null },
                note = note?.trim()?.ifEmpty { null },
                photoUri = photoUri,
                meterExpiryTime = expiry,
                isActive = true
            )
            parkingRepository.saveNewParking(entity)
            if (preferences.value.hapticFeedback) {
                HapticHelper.performSuccessHaptic(getApplication())
            }
            onSuccess()
        }
    }

    fun updateParkingSession(
        updated: ParkingEntity,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            parkingRepository.updateParking(updated)
            if (preferences.value.hapticFeedback) {
                HapticHelper.performClickHaptic(getApplication())
            }
            onSuccess()
        }
    }

    fun clearActiveParking(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            parkingRepository.clearActiveParking()
            if (preferences.value.hapticFeedback) {
                HapticHelper.performClickHaptic(getApplication())
            }
            onSuccess()
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            parkingRepository.deleteParking(id)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            parkingRepository.deleteAll()
        }
    }

    fun setDistanceUnit(unit: DistanceUnit) {
        preferencesRepository.setDistanceUnit(unit)
    }

    fun setHapticFeedback(enabled: Boolean) {
        preferencesRepository.setHapticFeedback(enabled)
    }

    fun setThemeMode(mode: AppThemeMode) {
        preferencesRepository.setThemeMode(mode)
    }

    fun setLanguage(language: AppLanguage) {
        preferencesRepository.setLanguage(language)
    }

    // Helper for saving photo from Uri into internal storage
    fun copyImageToInternalStorage(sourceUri: Uri): String? {
        return try {
            val context = getApplication<Application>()
            val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
            val destFile = File(photosDir, "parked_${System.currentTimeMillis()}.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { input: InputStream ->
                FileOutputStream(destFile).use { output: FileOutputStream ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun createTempCameraUri(): Pair<Uri, File> {
        val context = getApplication<Application>()
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val tempFile = File.createTempFile("capture_", ".jpg", imagesDir)
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, tempFile)
        return uri to tempFile
    }

    override fun onCleared() {
        super.onCleared()
        locationTracker.stopRealtimeTracking()
        compassManager.stopListening()
    }
}

class ParkingViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParkingViewModel::class.java)) {
            return ParkingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
