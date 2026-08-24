package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocationSearching
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import coil.compose.rememberAsyncImagePainter
import com.example.ui.components.LocationPermissionRationaleDialog
import com.example.ui.theme.ParkedBluePrimary
import com.example.ui.theme.ParkedEmerald
import com.example.util.DistanceFormatter
import com.example.viewmodel.ParkingViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveParkingScreen(
    viewModel: ParkingViewModel,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    var floor by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var spotNumber by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var photoUriString by remember { mutableStateOf<String?>(null) }
    var selectedMeterMinutes by remember { mutableStateOf<Int?>(null) }

    var currentTempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var currentTempCameraFile by remember { mutableStateOf<File?>(null) }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var accuracyMeters by remember { mutableStateOf<Float?>(null) }
    var isAcquiringGps by remember { mutableStateOf(true) }
    var showLocationRationaleDialog by remember { mutableStateOf(false) }

    fun refreshGpsLocation() {
        isAcquiringGps = true
        viewModel.acquireQuickLocation { loc ->
            isAcquiringGps = false
            if (loc != null) {
                latitude = loc.latitude
                longitude = loc.longitude
                accuracyMeters = loc.accuracy
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            refreshGpsLocation()
        } else {
            isAcquiringGps = false
        }
    }

    fun requestGpsWithRationale() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted) {
            refreshGpsLocation()
        } else {
            showLocationRationaleDialog = true
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentTempCameraUri != null) {
            val savedPath = viewModel.copyImageToInternalStorage(currentTempCameraUri!!)
            photoUriString = savedPath ?: currentTempCameraUri.toString()
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = viewModel.copyImageToInternalStorage(it)
            photoUriString = savedPath ?: it.toString()
        }
    }

    // Request Camera permission if needed
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val (uri, file) = viewModel.createTempCameraUri()
            currentTempCameraUri = uri
            currentTempCameraFile = file
            cameraLauncher.launch(uri)
        }
    }

    // Acquire GPS location on enter
    LaunchedEffect(Unit) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted) {
            viewModel.acquireQuickLocation { loc ->
                isAcquiringGps = false
                if (loc != null) {
                    latitude = loc.latitude
                    longitude = loc.longitude
                    accuracyMeters = loc.accuracy
                }
            }
        } else {
            isAcquiringGps = false
        }
    }

    val floorPresets = listOf("B2", "B1", "G", "1", "2", "3", "4", "Roof")
    val meterPresets = listOf(15, 30, 45, 60, 90, 120, 180)

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Save Parking Spot",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("save_parking_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Location Status Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gps_status_card"),
                shape = RoundedCornerShape(16.dp),
                color = if (latitude != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (latitude != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAcquiringGps) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (latitude != null) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "GPS Locked",
                            tint = ParkedEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.LocationSearching,
                            contentDescription = "No GPS",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAcquiringGps) "Acquiring GPS coordinates…"
                            else if (latitude != null) "GPS Coordinates Locked"
                            else "Indoor / Underground Mode",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isAcquiringGps) "Pinpointing exact position"
                            else if (latitude != null) DistanceFormatter.getAccuracyDescription(accuracyMeters)
                            else "No GPS signal. Floor and spot details will be used.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!isAcquiringGps) {
                        OutlinedButton(
                            onClick = { requestGpsWithRationale() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("enable_or_retry_gps_button")
                        ) {
                            Text(
                                text = if (latitude != null) "Refresh" else "Enable GPS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Photo Capture Section
            Text(
                text = "Parking Photo (Optional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (photoUriString != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    val imageSource = if (photoUriString!!.startsWith("content://") || photoUriString!!.startsWith("file://")) {
                        photoUriString
                    } else {
                        File(photoUriString!!)
                    }

                    Image(
                        painter = rememberAsyncImagePainter(model = imageSource),
                        contentDescription = "Captured Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        IconButton(
                            onClick = { photoUriString = null },
                            modifier = Modifier.testTag("delete_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Photo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val hasCamera = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasCamera) {
                                val (uri, file) = viewModel.createTempCameraUri()
                                currentTempCameraUri = uri
                                currentTempCameraFile = file
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("take_photo_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo")
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("pick_gallery_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose Photo")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Parking Spot Details Section Header
            Text(
                text = "Parking Spot Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Record level, aisle, bay number, and landmarks alongside GPS coordinates.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Level / Floor selector
            Text(
                text = "Level / Floor (Optional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                floorPresets.forEach { preset ->
                    FilterChip(
                        selected = floor == preset,
                        onClick = { floor = if (floor == preset) "" else preset },
                        label = { Text(preset, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = floor,
                onValueChange = { floor = it },
                label = { Text("Level / Floor Number") },
                placeholder = { Text("e.g. Level 2, Floor B1, P4") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("floor_input_field"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Aisle / Section & Spot / Space Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("Aisle / Section") },
                    placeholder = { Text("e.g. Aisle 4, Row B") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("section_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = spotNumber,
                    onValueChange = { spotNumber = it },
                    label = { Text("Spot / Space #") },
                    placeholder = { Text("e.g. 104, Bay 12") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("spot_number_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spot Details & Landmarks Notes field
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Spot Details, Landmarks & Notes") },
                placeholder = { Text("e.g. Next to elevator C, near pillar 42, green parking section") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input_field"),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Parking Meter Reminder
            Text(
                text = "Parking Meter Reminder (Optional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                meterPresets.forEach { mins ->
                    val labelText = if (mins < 60) "${mins}m" else "${mins / 60}h"
                    FilterChip(
                        selected = selectedMeterMinutes == mins,
                        onClick = {
                            selectedMeterMinutes = if (selectedMeterMinutes == mins) null else mins
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text(labelText) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Save Action
            Button(
                onClick = {
                    viewModel.saveParkingSession(
                        latitude = latitude,
                        longitude = longitude,
                        accuracyMeters = accuracyMeters,
                        floor = floor,
                        section = section,
                        spotNumber = spotNumber,
                        note = note,
                        photoUri = photoUriString,
                        meterMinutes = selectedMeterMinutes,
                        onSuccess = onSaved
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("confirm_save_parking_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Parking Spot",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
