package com.example.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Manages device orientation sensing by integrating the Accelerometer and Magnetic Field sensors.
 *
 * It fuses:
 * 1. Accelerometer (Sensor.TYPE_ACCELEROMETER) -> gravity vector
 * 2. Magnetic Field (Sensor.TYPE_MAGNETIC_FIELD) -> geomagnetic vector
 *
 * Uses SensorManager.getRotationMatrix and SensorManager.getOrientation to compute
 * the device's real-time azimuth (compass heading in degrees 0..360 relative to Magnetic North).
 */
class CompassManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _azimuthDegrees = MutableStateFlow<Float?>(null)
    val azimuthDegrees: StateFlow<Float?> = _azimuthDegrees.asStateFlow()

    private val _isSensorAvailable = MutableStateFlow(
        (accelerometer != null && magnetometer != null) || rotationVectorSensor != null
    )
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    private val _sensorAccuracy = MutableStateFlow(SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
    val sensorAccuracy: StateFlow<Int> = _sensorAccuracy.asStateFlow()

    // Sensor raw & low-pass filtered reading arrays
    private var gravityValues = FloatArray(3)
    private var geoMagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private val rotationMatrix = FloatArray(9)
    private val inclinationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var lastFilteredAzimuth = 0f
    private var isListening = false

    fun startListening() {
        if (isListening) return

        var registeredAny = false

        // Register Accelerometer and Magnetometer as primary sensor fusion pair
        if (accelerometer != null) {
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )
            registeredAny = true
        }

        if (magnetometer != null) {
            sensorManager.registerListener(
                this,
                magnetometer,
                SensorManager.SENSOR_DELAY_GAME
            )
            registeredAny = true
        }

        // Also register rotation vector if available as supplementary precision source
        if (rotationVectorSensor != null && (accelerometer == null || magnetometer == null)) {
            sensorManager.registerListener(
                this,
                rotationVectorSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
            registeredAny = true
        }

        isListening = registeredAny
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                // Low-pass filter for gravity / linear acceleration smoothing
                gravityValues = applyLowPassFilter(event.values, gravityValues, ALPHA_ACCEL)
                hasGravity = true
                calculateOrientationFromSensors()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                // Low-pass filter for geomagnetic noise
                geoMagneticValues = applyLowPassFilter(event.values, geoMagneticValues, ALPHA_MAGNET)
                hasGeomagnetic = true
                calculateOrientationFromSensors()
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                if (!hasGravity || !hasGeomagnetic) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    val azimuthRad = orientationAngles[0]
                    var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                    if (azimuthDeg < 0) azimuthDeg += 360f
                    applyFilteredAzimuth(azimuthDeg)
                }
            }
        }
    }

    private fun calculateOrientationFromSensors() {
        if (!hasGravity || !hasGeomagnetic) return

        // Compute rotation matrix R from gravity and geomagnetic vectors
        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            inclinationMatrix,
            gravityValues,
            geoMagneticValues
        )

        if (success) {
            // Compute orientation angles: [0] = Azimuth, [1] = Pitch, [2] = Roll
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuthRad = orientationAngles[0]
            var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            if (azimuthDeg < 0) azimuthDeg += 360f

            applyFilteredAzimuth(azimuthDeg)
        }
    }

    private fun applyFilteredAzimuth(newAzimuth: Float) {
        // Handle wrap-around for shortest angular distance interpolation (e.g., 359° to 1°)
        var diff = newAzimuth - lastFilteredAzimuth
        while (diff > 180f) diff -= 360f
        while (diff < -180f) diff += 360f

        // Exponential smoothing on the angle
        val smoothed = (lastFilteredAzimuth + diff * ALPHA_AZIMUTH + 360f) % 360f
        lastFilteredAzimuth = smoothed

        val currentVal = _azimuthDegrees.value
        if (currentVal == null || abs(smoothed - currentVal) > 0.3f) {
            _azimuthDegrees.value = smoothed
        }
    }

    /**
     * Exponential low-pass filter: y[i] = y[i] + alpha * (x[i] - y[i])
     */
    private fun applyLowPassFilter(input: FloatArray, output: FloatArray, alpha: Float): FloatArray {
        val result = output.clone()
        for (i in input.indices) {
            result[i] = result[i] + alpha * (input[i] - result[i])
        }
        return result
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD || sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            _sensorAccuracy.value = accuracy
        }
    }

    companion object {
        private const val ALPHA_ACCEL = 0.15f
        private const val ALPHA_MAGNET = 0.15f
        private const val ALPHA_AZIMUTH = 0.25f
    }
}

