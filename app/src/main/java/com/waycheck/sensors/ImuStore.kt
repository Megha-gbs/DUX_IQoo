package com.waycheck.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class ImuStore(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _heading = MutableStateFlow(0f)
    val heading: StateFlow<Float> = _heading.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var gravityValues: FloatArray? = null
    private var geoMagneticValues: FloatArray? = null

    // Circular low-pass filter accumulator (prevents compass jitter)
    private var smoothedSin = 0.0
    private var smoothedCos = 1.0
    private var isInitialized = false
    private val filterAlpha = 0.20 // 20% new reading, 80% history for smooth 60fps tracking

    fun start() {
        if (sensorManager == null) return

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
            _isAvailable.value = true
        } else {
            accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
            magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
            _isAvailable.value = accelerometer != null && magnetometer != null
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuthRad = orientationAngles[0].toDouble()
                updateSmoothedHeading(azimuthRad)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                gravityValues = event.values.clone()
                updateFallbackOrientation()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geoMagneticValues = event.values.clone()
                updateFallbackOrientation()
            }
        }
    }

    private fun updateSmoothedHeading(azimuthRad: Double) {
        val s = sin(azimuthRad)
        val c = cos(azimuthRad)

        if (!isInitialized) {
            smoothedSin = s
            smoothedCos = c
            isInitialized = true
        } else {
            smoothedSin = (1 - filterAlpha) * smoothedSin + filterAlpha * s
            smoothedCos = (1 - filterAlpha) * smoothedCos + filterAlpha * c
        }

        var deg = Math.toDegrees(atan2(smoothedSin, smoothedCos)).toFloat()
        deg = (deg + 360f) % 360f
        _heading.value = deg
    }

    private fun updateFallbackOrientation() {
        val g = gravityValues
        val m = geoMagneticValues
        if (g != null && m != null) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, g, m)) {
                val angles = FloatArray(3)
                SensorManager.getOrientation(r, angles)
                updateSmoothedHeading(angles[0].toDouble())
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
