package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.data.local.ParkingEntity
import com.example.ui.components.CompassView
import com.example.ui.components.LocationPermissionRationaleDialog
import com.example.ui.LocalizedCopy
import com.example.ui.components.PhotoViewerDialog
import com.example.ui.theme.ParkedAmber
import com.example.ui.theme.ParkedAmberDark
import com.example.ui.theme.ParkedBluePrimary
import com.example.ui.theme.ParkedEmerald
import com.example.util.DistanceFormatter
import com.example.util.NavigationLauncher
import com.example.util.TimeFormatter
import com.example.viewmodel.ParkingViewModel
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun CurrentParkingScreen(
    parking: ParkingEntity,
    viewModel: ParkingViewModel,
    onEditClick: () -> Unit,
    onCleared: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val navigationInfo by viewModel.navigationInfo.collectAsStateWithLifecycle()
    val isCompassAvailable by viewModel.isCompassAvailable.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val copy: (String) -> String = { key -> LocalizedCopy.get(preferences.language, key) }

    var showClearDialog by remember { mutableStateOf(false) }
    var viewingPhoto by remember { mutableStateOf(false) }
    var showLocationRationaleDialog by remember { mutableStateOf(false) }

    val hasFineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.startRealtimeTracking()
        }
    }

    // Live clock ticker for elapsed time & meter countdown
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        viewModel.startRealtimeTracking()
        while (true) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val hasCoordinates = parking.latitude != null && parking.longitude != null

    if (viewingPhoto && parking.photoUri != null) {
        PhotoViewerDialog(
            photoPathOrUri = parking.photoUri,
            onDismiss = { viewingPhoto = false }
        )
    }

    if (showLocationRationaleDialog) {
        LocationPermissionRationaleDialog(
            onConfirmRequest = {
                showLocationRationaleDialog = false
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onDismissOrSkip = {
                showLocationRationaleDialog = false
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ParkedEmerald,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = copy("clearSpot"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(copy("clearDescription"))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDialog = false
                        viewModel.clearActiveParking {
                            onCleared()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("confirm_clear_parking_dialog_button")
                ) {
                    Text(copy("yesBack"))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false },
                    modifier = Modifier.testTag("cancel_clear_parking_dialog_button")
                ) {
                    Text(com.example.ui.AppText.forLanguage(preferences.language).cancel)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("current_parking_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Parking Meter Timer Banner (if enabled)
        if (parking.meterExpiryTime != null) {
            val (meterText, isExpired) = TimeFormatter.formatMeterCountdown(
                parking.meterExpiryTime,
                currentTimeMillis
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("meter_countdown_banner"),
                shape = RoundedCornerShape(16.dp),
                color = if (isExpired) MaterialTheme.colorScheme.errorContainer
                else ParkedAmber.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isExpired) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    else ParkedAmberDark.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isExpired) Icons.Default.Warning else Icons.Default.Timer,
                        contentDescription = "Meter Alert",
                        tint = if (isExpired) MaterialTheme.colorScheme.error else ParkedAmberDark,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isExpired) "Parking Meter Expired" else "Parking Meter Timer",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpired) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = meterText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isExpired) MaterialTheme.colorScheme.error else ParkedAmberDark
                        )
                    }
                }
            }
        }

        // Radar & Compass Navigation Visualizer
        CompassView(
            navigationInfo = navigationInfo,
            isCompassAvailable = isCompassAvailable,
            distanceUnit = preferences.distanceUnit,
            hasGpsCoordinates = hasCoordinates,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (hasCoordinates && !hasFineLocation) {
            OutlinedButton(
                onClick = { showLocationRationaleDialog = true },
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("enable_live_compass_permission_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(copy("enableLocation"))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card with Spot Details & Actions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("parking_details_card"),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with Floor / Spot Title and Time Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    val floorText = if (!parking.floor.isNullOrBlank()) {
                        val trimmed = parking.floor.trim()
                        if (trimmed.startsWith("Floor", ignoreCase = true) || trimmed.startsWith("Level", ignoreCase = true)) {
                            trimmed
                        } else {
                            "Level $trimmed"
                        }
                    } else "Parked Vehicle"

                    val sectionSpotText = buildString {
                        if (!parking.section.isNullOrBlank()) {
                            val trimmedSec = parking.section.trim()
                            if (trimmedSec.startsWith("Aisle", ignoreCase = true) ||
                                trimmedSec.startsWith("Section", ignoreCase = true) ||
                                trimmedSec.startsWith("Row", ignoreCase = true) ||
                                trimmedSec.startsWith("Zone", ignoreCase = true)
                            ) {
                                append(trimmedSec)
                            } else {
                                append("Aisle / Section: $trimmedSec")
                            }
                        }
                        if (!parking.section.isNullOrBlank() && !parking.spotNumber.isNullOrBlank()) append(" • ")
                        if (!parking.spotNumber.isNullOrBlank()) {
                            val trimmedSpot = parking.spotNumber.trim()
                            if (trimmedSpot.startsWith("Spot", ignoreCase = true) ||
                                trimmedSpot.startsWith("Bay", ignoreCase = true) ||
                                trimmedSpot.startsWith("Space", ignoreCase = true)
                            ) {
                                append(trimmedSpot)
                            } else {
                                append("Spot #$trimmedSpot")
                            }
                        }
                        if (isEmpty() && parking.floor.isNullOrBlank()) append("Saved Location")
                    }

                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = floorText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (sectionSpotText.isNotBlank()) {
                            Text(
                                text = sectionSpotText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!parking.note.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "\"${parking.note}\"",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Minimalist Time Badge
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = TimeFormatter.formatElapsedDuration(parking.timestamp, currentTimeMillis),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Parking Photo (if exists)
                if (parking.photoUri != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewingPhoto = true }
                            .testTag("parking_photo_thumbnail")
                    ) {
                        val imageSource = if (parking.photoUri.startsWith("content://") || parking.photoUri.startsWith("file://")) {
                            parking.photoUri
                        } else {
                            File(parking.photoUri)
                        }

                        Image(
                            painter = rememberAsyncImagePainter(model = imageSource),
                            contentDescription = "Parking Spot Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.65f)
                        ) {
                            Text(
                            text = copy("tapToEnlarge"),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons Grid (Route & Photo/Edit)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Route button
                    Button(
                        onClick = {
                            if (hasCoordinates) {
                                NavigationLauncher.openWalkingDirections(
                                    context = context,
                                    latitude = parking.latitude!!,
                                    longitude = parking.longitude!!,
                                    label = "My Parked Car"
                                )
                            }
                        },
                        enabled = hasCoordinates,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("walking_directions_button"),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = copy("route"),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }

                    // Edit button
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("edit_parking_button"),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                        text = copy("editSpot"),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Clear Location Destructive Button
                TextButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("clear_parking_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = copy("clearLocation"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Accuracy and timestamp caption
                if (hasCoordinates) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${DistanceFormatter.getAccuracyDescription(parking.accuracyMeters)} · Saved ${TimeFormatter.formatParkedTime(parking.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
