package com.example.util

import com.example.data.local.DistanceUnit
import java.util.Locale

object DistanceFormatter {

    fun formatDistance(meters: Float, unit: DistanceUnit): String {
        return when (unit) {
            DistanceUnit.METRIC -> {
                if (meters < 1000) {
                    "${meters.toInt()} m"
                } else {
                    String.format(Locale.getDefault(), "%.1f km", meters / 1000f)
                }
            }
            DistanceUnit.IMPERIAL -> {
                val feet = meters * 3.28084f
                if (feet < 1000) {
                    "${feet.toInt()} ft"
                } else {
                    val miles = meters / 1609.344f
                    String.format(Locale.getDefault(), "%.2f mi", miles)
                }
            }
        }
    }

    fun getAccuracyDescription(accuracyMeters: Float?): String {
        if (accuracyMeters == null) return "Unknown accuracy"
        return when {
            accuracyMeters <= 5f -> "High accuracy (±${accuracyMeters.toInt()}m)"
            accuracyMeters <= 15f -> "Good accuracy (±${accuracyMeters.toInt()}m)"
            accuracyMeters <= 35f -> "Moderate accuracy (±${accuracyMeters.toInt()}m)"
            else -> "Low accuracy (±${accuracyMeters.toInt()}m)"
        }
    }
}
