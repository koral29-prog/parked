package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeFormatter {

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val fullDateTimeFormat = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    fun formatParkedTime(timestamp: Long): String {
        return fullDateTimeFormat.format(Date(timestamp))
    }

    fun formatShortTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatElapsedDuration(parkedTimestamp: Long, now: Long = System.currentTimeMillis()): String {
        val diffMillis = (now - parkedTimestamp).coerceAtLeast(0)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min${if (minutes == 1L) "" else "s"} ago"
            hours < 24 -> {
                val remMinutes = minutes % 60
                if (remMinutes > 0) "${hours}h ${remMinutes}m ago" else "${hours}h ago"
            }
            else -> {
                val remHours = hours % 24
                if (remHours > 0) "${days}d ${remHours}h ago" else "${days}d ago"
            }
        }
    }

    fun formatMeterCountdown(expiryTime: Long, now: Long = System.currentTimeMillis()): Pair<String, Boolean> {
        val diffMillis = expiryTime - now
        val isExpired = diffMillis <= 0
        val absMillis = Math.abs(diffMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(absMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(absMillis)

        val formatted = when {
            minutes < 60 -> "${minutes}m"
            else -> {
                val remMinutes = minutes % 60
                "${hours}h ${remMinutes}m"
            }
        }

        return if (isExpired) {
            "Expired $formatted ago" to true
        } else {
            "$formatted left" to false
        }
    }
}
