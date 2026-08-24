package com.example

import com.example.data.local.DistanceUnit
import com.example.data.location.LocationTracker
import com.example.util.DistanceFormatter
import com.example.util.TimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ParkingUtilsTest {

    @Test
    fun testDistanceFormatterMetric() {
        assertEquals("45 m", DistanceFormatter.formatDistance(45f, DistanceUnit.METRIC))
        assertEquals("1.2 km", DistanceFormatter.formatDistance(1200f, DistanceUnit.METRIC))
    }

    @Test
    fun testDistanceFormatterImperial() {
        val formattedMetersAsFeet = DistanceFormatter.formatDistance(30f, DistanceUnit.IMPERIAL)
        assertTrue(formattedMetersAsFeet.contains("ft"))

        val formattedLongDistance = DistanceFormatter.formatDistance(2000f, DistanceUnit.IMPERIAL)
        assertTrue(formattedLongDistance.contains("mi"))
    }

    @Test
    fun testTimeFormatterElapsed() {
        val now = System.currentTimeMillis()
        assertEquals("Just now", TimeFormatter.formatElapsedDuration(now, now))
        assertEquals("10 mins ago", TimeFormatter.formatElapsedDuration(now - 10 * 60 * 1000L, now))
    }

    @Test
    fun testNavigationHeadingCalculation() {
        // User at (0, 0), Target at (1.0, 0.0) -> True North (bearing = 0 degrees)
        // Device pointing North (azimuth = 0) -> Relative heading should be 0 degrees (pointing straight ahead)
        val navNorthFacingNorth = LocationTracker.calculateNavigation(
            userLat = 0.0,
            userLng = 0.0,
            targetLat = 1.0,
            targetLng = 0.0,
            deviceAzimuth = 0f
        )
        assertEquals(0f, navNorthFacingNorth.bearingDegrees, 1.0f)
        assertEquals(0f, navNorthFacingNorth.relativeHeadingDegrees, 1.0f)

        // Device pointing East (azimuth = 90 degrees) while target is North (bearing = 0 degrees)
        // Relative heading should be 270 degrees (pointing left)
        val navNorthFacingEast = LocationTracker.calculateNavigation(
            userLat = 0.0,
            userLng = 0.0,
            targetLat = 1.0,
            targetLng = 0.0,
            deviceAzimuth = 90f
        )
        assertEquals(0f, navNorthFacingEast.bearingDegrees, 1.0f)
        assertEquals(270f, navNorthFacingEast.relativeHeadingDegrees, 1.0f)
    }

    @Test
    fun testParkingEntityWithAisleLevelAndSpot() {
        val parking = com.example.data.local.ParkingEntity(
            id = 1,
            latitude = 37.7749,
            longitude = -122.4194,
            accuracyMeters = 4.2f,
            floor = "Level 3",
            section = "Aisle 4B",
            spotNumber = "104",
            note = "Near north elevator and blue pillar"
        )

        assertEquals("Level 3", parking.floor)
        assertEquals("Aisle 4B", parking.section)
        assertEquals("104", parking.spotNumber)
        assertEquals("Near north elevator and blue pillar", parking.note)
        assertEquals(37.7749, parking.latitude!!, 0.0001)
        assertEquals(-122.4194, parking.longitude!!, 0.0001)
    }
}


