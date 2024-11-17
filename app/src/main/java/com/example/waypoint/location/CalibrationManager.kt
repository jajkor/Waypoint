package com.example.waypoint.location

import android.content.Context
import android.content.SharedPreferences
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.log10

data class DistanceReadings(
    val distance: Double,
    val rssiValues: List<Int>,
    val timestamp: Long = System.currentTimeMillis()
)

data class CalibrationData(
    val referenceRSSI: Int,
    val environmentalFactor: Double,
    val lastCalibrationDate: Long = System.currentTimeMillis(),
    val distanceReadings: Map<Double, DistanceReadings> = mapOf()
)

class CalibrationManager(
    context: Context
) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(
            "wifi_calibration",
            Context.MODE_PRIVATE
        )
    private val gson = Gson()
    private val wifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // Modified to shorter distances (in meters)
    private val calibrationDistances = listOf(0.5, 1.0, 2.0) // Changed from 1.0, 3.0, 5.0
    private val readingsPerDistance = 5
    private val scanInterval = 1000L // 1 second between scans

    companion object {
        const val CALIBRATION_VALIDITY_PERIOD = 30L * 24 * 60 * 60 * 1000 // 30 days
    }

    fun startCalibration(
        accessPoint: AccessPoint,
        currentDistance: Double,
        onProgress: (Int, String) -> Unit,
        onDistanceComplete: (Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (currentDistance !in calibrationDistances) {
            onError("Invalid calibration distance: $currentDistance")
            return
        }

        val readings = mutableListOf<Int>()
        var scanCount = 0

        val handler = Handler(Looper.getMainLooper())
        val scanRunnable =
            object : Runnable {
                override fun run() {
                    if (scanCount < readingsPerDistance) {
                        val success = wifiManager.startScan()
                        if (success) {
                            val results = wifiManager.scanResults
                            results.find { it.BSSID == accessPoint.bssid }?.let { result ->
                                readings.add(result.level)
                                scanCount++

                                val progress = (scanCount * 100) / readingsPerDistance
                                onProgress(
                                    progress,
                                    "Collecting reading $scanCount of $readingsPerDistance at ${currentDistance}m"
                                )

                                handler.postDelayed(this, scanInterval)
                            } ?: run {
                                onError("AP not found in scan results")
                            }
                        } else {
                            onError("Failed to start WiFi scan")
                        }
                    } else {
                        saveDistanceReadings(accessPoint.bssid, currentDistance, readings)
                        onDistanceComplete(currentDistance)
                    }
                }
            }

        handler.post(scanRunnable)
    }

    private fun saveDistanceReadings(
        bssid: String,
        distance: Double,
        readings: List<Int>
    ) {
        Log.d("CalibrationManager", "Saving readings for distance $distance: $readings")

        // Validate input
        if (distance <= 0) {
            Log.e("CalibrationManager", "Invalid distance: $distance")
            return
        }

        if (readings.isEmpty()) {
            Log.e("CalibrationManager", "No readings to save")
            return
        }

        val existingData = getCalibrationData(bssid)
        val existingReadings = existingData?.distanceReadings ?: mapOf()

        try {
            val updatedReadings = existingReadings.toMutableMap()
            updatedReadings[distance] =
                DistanceReadings(
                    distance = distance,
                    rssiValues = readings.filter { it < 0 } // RSSI values should always be negative
                )

            // Calculate new calibration parameters
            val (referenceRSSI, environmentalFactor) = calculateEnvironmentalFactorForShortDistances(updatedReadings)

            // Validate calculated values
            if (!environmentalFactor.isFinite() || environmentalFactor !in 1.0..4.0) {
                Log.e("CalibrationManager", "Invalid environmental factor calculated: $environmentalFactor")
                return
            }

            val newCalibrationData =
                CalibrationData(
                    referenceRSSI = referenceRSSI,
                    environmentalFactor = environmentalFactor,
                    distanceReadings = updatedReadings
                )

            // Log before saving
            Log.d(
                "CalibrationManager",
                """
                Saving calibration data for $bssid:
                Reference RSSI: $referenceRSSI
                Environmental Factor: $environmentalFactor
                Number of readings: ${updatedReadings.size}
                """.trimIndent()
            )

            saveCalibrationData(bssid, newCalibrationData)
        } catch (e: Exception) {
            Log.e("CalibrationManager", "Error saving distance readings", e)
        }
    }

    // Modified calculation method optimized for shorter distances
    private fun calculateEnvironmentalFactorForShortDistances(readings: Map<Double, DistanceReadings>): Pair<Int, Double> {
        // Get reference RSSI (at shortest distance)
        val referenceRSSI = readings[0.5]?.rssiValues?.average()?.toInt() ?: -45

        var sumFactor = 0.0
        var validMeasurements = 0

        readings.forEach { (distance, distanceReadings) ->
            // Only process distances greater than our reference distance
            if (distance > 0.5) {
                val avgRSSI = distanceReadings.rssiValues.average()

                try {
                    // Ensure distance is valid for log calculation
                    if (distance > 0) {
                        val logDistance = log10(distance)
                        // Avoid division by very small numbers
                        if (logDistance.absoluteValue > 0.001) {
                            val pathLoss = abs(avgRSSI) - abs(referenceRSSI)
                            val factor = pathLoss / (10.0 * logDistance)

                            // Only include reasonable values
                            if (factor.isFinite() && factor in 1.0..4.0) {
                                sumFactor += factor
                                validMeasurements++

                                // Log valid measurements for debugging
                                Log.d(
                                    "CalibrationManager",
                                    """
                                    Distance: $distance
                                    Avg RSSI: $avgRSSI
                                    Path Loss: $pathLoss
                                    Factor: $factor
                                    """.trimIndent()
                                )
                            } else {
                                Log.w("CalibrationManager", "Invalid factor calculated: $factor")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CalibrationManager", "Error calculating factor for distance $distance", e)
                }
            }
        }

        // Use a reasonable default if no valid measurements
        val environmentalFactor =
            if (validMeasurements > 0) {
                (sumFactor / validMeasurements).coerceIn(1.0, 4.0)
            } else {
                2.0 // Default value for indoor environments
            }

        Log.d("CalibrationManager", "Final environmental factor: $environmentalFactor")
        return Pair(referenceRSSI, environmentalFactor)
    }

    fun saveCalibrationData(
        bssid: String,
        data: CalibrationData
    ) {
        preferences.edit().putString(bssid, gson.toJson(data)).apply()
    }

    fun getCalibrationData(bssid: String): CalibrationData? {
        val json = preferences.getString(bssid, null)
        return json?.let {
            gson.fromJson(it, CalibrationData::class.java)
        }
    }

    fun getAllCalibrationData(): Map<String, CalibrationData> =
        preferences.all
            .mapNotNull { (bssid, json) ->
                if (json is String) {
                    bssid to gson.fromJson(json, CalibrationData::class.java)
                } else {
                    null
                }
            }.toMap()

    fun needsCalibration(bssid: String): Boolean {
        val data = getCalibrationData(bssid)
        val currentTime = System.currentTimeMillis()

        return data == null ||
            (currentTime - data.lastCalibrationDate > CALIBRATION_VALIDITY_PERIOD) ||
            !hasAllDistances(data)
    }

    private fun hasAllDistances(data: CalibrationData): Boolean =
        calibrationDistances.all { distance ->
            data.distanceReadings.containsKey(distance)
        }
}
