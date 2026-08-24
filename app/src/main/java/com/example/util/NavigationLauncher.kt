package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object NavigationLauncher {

    fun openWalkingDirections(context: Context, latitude: Double, longitude: Double, label: String = "Parked Car") {
        try {
            // Priority 1: Google Maps Walking Navigation Intent
            val navUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=w")
            val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                setPackage("com.google.android.apps.maps")
            }

            if (navIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(navIntent)
                return
            }

            // Priority 2: Generic geo URI with walking/pin query
            val encodedLabel = Uri.encode(label)
            val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)

            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
                return
            }

            // Priority 3: Browser fallback to Google Maps walking route
            val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude&travelmode=walking")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open maps application.", Toast.LENGTH_SHORT).show()
        }
    }
}
