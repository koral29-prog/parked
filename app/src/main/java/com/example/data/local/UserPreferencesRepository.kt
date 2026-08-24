package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DistanceUnit {
    METRIC,    // Meters / Kilometers
    IMPERIAL   // Feet / Miles
}

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage {
    ENGLISH,
    TURKISH
}

data class UserPreferences(
    val distanceUnit: DistanceUnit = DistanceUnit.METRIC,
    val hapticFeedback: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val highAccuracyCompass: Boolean = true,
    val language: AppLanguage = AppLanguage.ENGLISH
)

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("parked_user_preferences", Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        val unitStr = prefs.getString("distance_unit", DistanceUnit.METRIC.name) ?: DistanceUnit.METRIC.name
        val themeStr = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val haptics = prefs.getBoolean("haptic_feedback", true)
        val highAccuracy = prefs.getBoolean("high_accuracy_compass", true)
        val languageStr = prefs.getString("language", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name

        val unit = try {
            DistanceUnit.valueOf(unitStr)
        } catch (e: Exception) {
            DistanceUnit.METRIC
        }

        val theme = try {
            AppThemeMode.valueOf(themeStr)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }

        val language = try {
            AppLanguage.valueOf(languageStr)
        } catch (e: Exception) {
            AppLanguage.ENGLISH
        }

        return UserPreferences(
            distanceUnit = unit,
            hapticFeedback = haptics,
            themeMode = theme,
            highAccuracyCompass = highAccuracy,
            language = language
        )
    }

    fun setDistanceUnit(unit: DistanceUnit) {
        prefs.edit().putString("distance_unit", unit.name).apply()
        _preferences.value = _preferences.value.copy(distanceUnit = unit)
    }

    fun setHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_feedback", enabled).apply()
        _preferences.value = _preferences.value.copy(hapticFeedback = enabled)
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _preferences.value = _preferences.value.copy(themeMode = mode)
    }

    fun setHighAccuracyCompass(enabled: Boolean) {
        prefs.edit().putBoolean("high_accuracy_compass", enabled).apply()
        _preferences.value = _preferences.value.copy(highAccuracyCompass = enabled)
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("language", language.name).apply()
        _preferences.value = _preferences.value.copy(language = language)
    }
}
