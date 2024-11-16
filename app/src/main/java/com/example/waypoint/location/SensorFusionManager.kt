package com.example.waypoint.location

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.waypoint.Vector2
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class SensorFusionManager(
    private val context: Context,
    private val onPositionUpdate: (Vector2) -> Unit,
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val kalmanFilter = KalmanFilter()
    private var lastWifiPosition: Vector2? = null
    private var lastStepTimestamp = 0L

    // Motion detection parameters
    private var lastAcceleration = FloatArray(3)
    private val stepThreshold = 10.0f
    private val minStepInterval = 250L // milliseconds
    private val stepLength = 0.65 // meters

    // Orientation
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var currentHeading = 0.0f
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    init {
        registerSensors()
    }

    fun registerSensors() {
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL,
        )
        sensorManager.registerListener(
            this,
            magnetometer,
            SensorManager.SENSOR_DELAY_NORMAL,
        )
        sensorManager.registerListener(
            this,
            gyroscope,
            SensorManager.SENSOR_DELAY_NORMAL,
        )
    }

    fun unregisterSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerReading, 0, 3)
                detectStep(event.values)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetometerReading, 0, 3)
                updateOrientation()
            }
            Sensor.TYPE_GYROSCOPE -> {
                updateGyroscope(event.values)
            }
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int,
    ) {}

    private fun detectStep(acceleration: FloatArray) {
        val magnitude =
            sqrt(
                acceleration[0] * acceleration[0] +
                    acceleration[1] * acceleration[1] +
                    acceleration[2] * acceleration[2],
            )

        val delta =
            magnitude -
                sqrt(
                    lastAcceleration[0] * lastAcceleration[0] +
                        lastAcceleration[1] * lastAcceleration[1] +
                        lastAcceleration[2] * lastAcceleration[2],
                )

        System.arraycopy(acceleration, 0, lastAcceleration, 0, 3)

        if (delta > stepThreshold) {
            val timestamp = System.currentTimeMillis()
            if (timestamp - lastStepTimestamp > minStepInterval) {
                lastStepTimestamp = timestamp
                onStepDetected()
            }
        }
    }

    private fun updateOrientation() {
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading,
        )

        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        currentHeading = orientationAngles[0]
    }

    private fun updateGyroscope(gyroValues: FloatArray) {
        // Use gyroscope data to improve heading accuracy
        val gyroHeading = atan2(gyroValues[1], gyroValues[0])
        currentHeading = 0.98f * (currentHeading + gyroValues[2] * 0.02f) + 0.02f * gyroHeading
    }

    private fun onStepDetected() {
        // Calculate position change based on step and heading
        val dx = stepLength * cos(currentHeading.toDouble())
        val dy = stepLength * sin(currentHeading.toDouble())

        // Get current position from Kalman filter
        val currentPosition = kalmanFilter.getPosition()

        // Update with dead reckoning
        val deadReckoningPosition =
            Vector2(
                currentPosition.x + dx,
                currentPosition.y + dy,
            )

        // Update Kalman filter with dead reckoning position
        kalmanFilter.predict()
        kalmanFilter.update(deadReckoningPosition)

        // Notify position update
        onPositionUpdate(kalmanFilter.getPosition())
    }

    fun processWifiPosition(wifiPosition: Vector2) {
        lastWifiPosition = wifiPosition

        // Update Kalman filter with WiFi position
        kalmanFilter.update(wifiPosition)

        // Get fused position
        val fusedPosition = kalmanFilter.getPosition()

        // Log positions for debugging
        Log.d(
            "SensorFusion",
            """
            WiFi Position: $wifiPosition
            Fused Position: $fusedPosition
            """.trimIndent(),
        )

        // Notify position update
        onPositionUpdate(fusedPosition)
    }
}
