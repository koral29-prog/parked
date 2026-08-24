package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DistanceUnit
import com.example.data.location.NavigationInfo
import com.example.ui.theme.CleanBorderDashed
import com.example.ui.theme.CleanBorderSubtle
import com.example.ui.theme.CleanGreenPrimary
import com.example.ui.theme.CleanTextMutedLight
import com.example.ui.theme.CleanTextSecondaryLight
import com.example.util.DistanceFormatter

@Composable
fun CompassView(
    navigationInfo: NavigationInfo?,
    isCompassAvailable: Boolean,
    distanceUnit: DistanceUnit,
    hasGpsCoordinates: Boolean,
    modifier: Modifier = Modifier
) {
    val relativeAngle = navigationInfo?.relativeHeadingDegrees ?: 0f
    val animatedAngle by animateFloatAsState(
        targetValue = relativeAngle,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
        label = "compassRotation"
    )

    Column(
        modifier = modifier.testTag("compass_view_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Minimalist Radar / Compass Dial
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            val outerDashedColor = MaterialTheme.colorScheme.outline
            val innerRingColor = MaterialTheme.colorScheme.outlineVariant
            val forwardNeedleColor = MaterialTheme.colorScheme.primary
            val reverseNeedleColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.width / 2
                val center = Offset(size.width / 2, size.height / 2)

                // Dashed outer ring
                drawCircle(
                    color = outerDashedColor,
                    radius = radius - 4.dp.toPx(),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    )
                )

                // Inner solid ring
                drawCircle(
                    color = innerRingColor,
                    radius = radius * 0.76f,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            if (hasGpsCoordinates) {
                // Two-tone Minimalist Compass Pointer (Target needle / Reverse needle)
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .rotate(animatedAngle),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(110.dp)) {
                        val w = size.width
                        val h = size.height
                        val midX = w / 2
                        val midY = h / 2

                        // Forward needle (points to car in theme primary color)
                        val topNeedlePath = Path().apply {
                            moveTo(midX, 6f)                    // Tip
                            lineTo(midX - 16f, midY)            // Left wing
                            lineTo(midX, midY - 14f)            // Inner tuck
                            lineTo(midX + 16f, midY)            // Right wing
                            close()
                        }
                        drawPath(path = topNeedlePath, color = forwardNeedleColor)

                        // Tail needle (reverse needle)
                        val bottomNeedlePath = Path().apply {
                            moveTo(midX, h - 6f)                // Bottom Tip
                            lineTo(midX - 16f, midY)            // Left wing
                            lineTo(midX, midY + 14f)            // Inner tuck
                            lineTo(midX + 16f, midY)            // Right wing
                            close()
                        }
                        drawPath(path = bottomNeedlePath, color = reverseNeedleColor)
                    }
                }
            } else {
                // Indoor / Underground mode
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Car Spot",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Indoor Spot",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Minimalist Distance & Cardinal Direction
        if (hasGpsCoordinates && navigationInfo != null) {
            val dist = navigationInfo.distanceMeters
            val (numText, unitText) = when (distanceUnit) {
                DistanceUnit.METRIC -> {
                    if (dist < 1000f) {
                        dist.toInt().toString() to "m"
                    } else {
                        String.format("%.1f", dist / 1000f) to "km"
                    }
                }
                DistanceUnit.IMPERIAL -> {
                    val feet = dist * 3.28084f
                    if (feet < 1000f) {
                        feet.toInt().toString() to "ft"
                    } else {
                        String.format("%.1f", feet / 5280f) to "mi"
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.testTag("distance_text")
            ) {
                Text(
                    text = numText,
                    fontSize = 58.sp,
                    lineHeight = 58.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-2).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unitText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Direction label with clean tracking
            val bearingDeg = navigationInfo.bearingDegrees
            val directionName = getCardinalDirection(bearingDeg)

            Text(
                text = directionName.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp,
                color = MaterialTheme.colorScheme.primary
            )

            if (!isCompassAvailable) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Compass offline · map bearing",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        } else if (hasGpsCoordinates) {
            Text(
                text = "Acquiring live position…",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = "Saved without GPS (Indoor Level)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun getCardinalDirection(degrees: Float): String {
    val normalized = (degrees % 360 + 360) % 360
    return when {
        normalized >= 337.5 || normalized < 22.5 -> "North"
        normalized >= 22.5 && normalized < 67.5 -> "North East"
        normalized >= 67.5 && normalized < 112.5 -> "East"
        normalized >= 112.5 && normalized < 157.5 -> "South East"
        normalized >= 157.5 && normalized < 202.5 -> "South"
        normalized >= 202.5 && normalized < 247.5 -> "South West"
        normalized >= 247.5 && normalized < 292.5 -> "West"
        else -> "North West"
    }
}

