package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.data.local.ParkingEntity
import com.example.ui.components.LocationPermissionRationaleDialog
import com.example.viewmodel.ParkingViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditParkingScreen(
    parking: ParkingEntity,
    viewModel: ParkingViewModel,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    var floor by remember { mutableStateOf(parking.floor ?: "") }
    var section by remember { mutableStateOf(parking.section ?: "") }
    var spotNumber by remember { mutableStateOf(parking.spotNumber ?: "") }
    var note by remember { mutableStateOf(parking.note ?: "") }
    var photoUriString by remember { mutableStateOf<String?>(parking.photoUri) }

    var latitude by remember { mutableStateOf(parking.latitude) }
    var longitude by remember { mutableStateOf(parking.longitude) }
    var accuracyMeters by remember { mutableStateOf(parking.accuracyMeters) }
    var isRefreshingGps by remember { mutableStateOf(false) }
    var showLocationRationaleDialog by remember { mutableStateOf(false) }

    fun refreshGps() {
        isRefreshingGps = true
        viewModel.acquireQuickLocation { loc ->
            isRefreshingGps = false
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
            refreshGps()
        } else {
            isRefreshingGps = false
        }
    }

    fun requestGpsWithRationale() {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted) {
            refreshGps()
        } else {
            showLocationRationaleDialog = true
        }
    }

    var currentTempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var currentTempCameraFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentTempCameraUri != null) {
            val savedPath = viewModel.copyImageToInternalStorage(currentTempCameraUri!!)
            photoUriString = savedPath ?: currentTempCameraUri.toString()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = viewModel.copyImageToInternalStorage(it)
            photoUriString = savedPath ?: it.toString()
        }
    }

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

    val floorPresets = listOf("B2", "B1", "G", "1", "2", "3", "4", "Roof")

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
                        text = "Edit Parking Details",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("edit_parking_back_button")
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
            // GPS Location Refresher
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GPS Coordinates",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (latitude != null) "${String.format("%.5f", latitude)}, ${String.format("%.5f", longitude)}"
                            else "No coordinates recorded",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { requestGpsWithRationale() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("refresh_gps_button")
                    ) {
                        if (isRefreshingGps) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Update GPS")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Photo section
            Text(
                text = "Parking Photo",
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
                            modifier = Modifier.testTag("edit_delete_photo_button")
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
                            .testTag("edit_take_photo_button"),
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
                            .testTag("edit_pick_gallery_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose Photo")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Floor Presets
            Text(
                text = "Floor / Level",
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
                    .testTag("edit_floor_input_field"),
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
                        .testTag("edit_section_input_field"),
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
                        .testTag("edit_spot_number_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spot Details & Landmarks Notes
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
                    .testTag("edit_note_input_field"),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save Changes button
            Button(
                onClick = {
                    val updated = parking.copy(
                        latitude = latitude,
                        longitude = longitude,
                        accuracyMeters = accuracyMeters,
                        floor = floor.trim().ifEmpty { null },
                        section = section.trim().ifEmpty { null },
                        spotNumber = spotNumber.trim().ifEmpty { null },
                        note = note.trim().ifEmpty { null },
                        photoUri = photoUriString
                    )
                    viewModel.updateParkingSession(updated) {
                        onSaved()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_edited_parking_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Changes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
