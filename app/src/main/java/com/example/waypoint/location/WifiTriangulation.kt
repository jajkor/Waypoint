package com.example.waypoint.location

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.waypoint.Vector2
import kotlin.math.*

class WifiTriangulation(
    private val context: Context,
    data: LocationData
) {
    private val knownAccessPoints: MutableMap<String, AccessPoint>
    private val calibrationManager = CalibrationManager(context)
    private val rssiHistory = mutableMapOf<String, ArrayDeque<Int>>()
    private val historySize = 5

    // Distance calibration parameters
    private val distanceCalibrationFactors =
        mapOf(
            "VERY_CLOSE" to 1.2, // 0-3 meters
            "CLOSE" to 1.5, // 3-5 meters
            "MEDIUM" to 1.8, // 5-10 meters
            "FAR" to 2.2 // >10 meters
        )

    init {
        // Load calibrated values or use defaults from JSON
        knownAccessPoints =
            data.accessPoints
                .associate { ap ->
                    val calibrationData = calibrationManager.getCalibrationData(ap.bssid)
                    val calibratedAP =
                        if (calibrationData != null) {
                            // Check if calibration is still valid
                            val isCalibrationValid =
                                (System.currentTimeMillis() - calibrationData.lastCalibrationDate) <
                                    CalibrationManager.CALIBRATION_VALIDITY_PERIOD

                            if (isCalibrationValid) {
                                ap.copy(
                                    referenceRSSI = calibrationData.referenceRSSI,
                                    environmentalFactor = calibrationData.environmentalFactor
                                )
                            } else {
                                ap
                            }
                        } else {
                            ap
                        }

                    ap.bssid to calibratedAP
                }.toMutableMap()

        // Initialize RSSI history
        knownAccessPoints.keys.forEach { bssid ->
            rssiHistory[bssid] = ArrayDeque(historySize)
        }
    }

    fun needsCalibration(): Boolean {
        val calibrationData = calibrationManager.getAllCalibrationData()
        val currentTime = System.currentTimeMillis()

        return knownAccessPoints.any { (bssid, _) ->
            val data = calibrationData[bssid]
            data == null ||
                (currentTime - data.lastCalibrationDate) >
                CalibrationManager.CALIBRATION_VALIDITY_PERIOD
        }
    }

    fun saveCalibration(
        bssid: String,
        referenceRSSI: Int,
        environmentalFactor: Double
    ) {
        val calibrationData =
            CalibrationData(
                referenceRSSI = referenceRSSI,
                environmentalFactor = environmentalFactor
            )
        calibrationManager.saveCalibrationData(bssid, calibrationData)

        // Update current AP data
        knownAccessPoints[bssid]?.let { ap ->
            knownAccessPoints[bssid] =
                ap.copy(
                    referenceRSSI = referenceRSSI,
                    environmentalFactor = environmentalFactor
                )
        }
    }

    /**
     * Improved distance calculation with dynamic calibration
     */
    private fun calculateDistance(
        rssi: Int,
        referenceRssi: Int,
        environmentalFactor: Double,
        bssid: String
    ): Double {
        // Add RSSI to history
        rssiHistory[bssid]?.apply {
            if (size >= historySize) removeFirst()
            addLast(rssi)
        }

        // Use median RSSI for more stable readings
        val medianRssi = rssiHistory[bssid]?.sorted()?.getOrNull(historySize / 2) ?: rssi

        // Calculate basic distance
        val rawDistance = 10.0.pow((abs(medianRssi) - abs(referenceRssi)) / (10.0 * environmentalFactor))

        // Apply distance-based calibration
        val calibrationFactor =
            when {
                rawDistance < 3.0 -> distanceCalibrationFactors["VERY_CLOSE"]
                rawDistance < 5.0 -> distanceCalibrationFactors["CLOSE"]
                rawDistance < 10.0 -> distanceCalibrationFactors["MEDIUM"]
                else -> distanceCalibrationFactors["FAR"]
            } ?: 1.0

        return rawDistance * calibrationFactor
    }

    /**
     * Weight calculation based on RSSI reliability
     */
    private fun calculateWeight(rssi: Int): Double {
        // Stronger signals get higher weights
        return when {
            rssi > -50 -> 1.0
            rssi > -60 -> 0.8
            rssi > -70 -> 0.6
            rssi > -80 -> 0.4
            else -> 0.2
        }
    }

    /**
     * Process RSSI readings and estimate position
     */
    fun estimatePosition(rssiReadings: Map<String, Int>): Vector2? {
        val distances =
            rssiReadings
                .mapNotNull { (bssid, rssi) ->
                    knownAccessPoints[bssid]?.let { ap ->
                        val distance = calculateDistance(rssi, ap.referenceRSSI, ap.environmentalFactor, bssid)
                        val weight = calculateWeight(rssi)
                        Triple(ap, distance, weight)
                    }
                }.sortedByDescending { it.third } // Sort by weight
                .take(3) // Use only the 3 most reliable readings
                .associate { it.first to it.second }

        val position = calculatePosition(distances)

        // Log detailed information for debugging
        position?.let {
            Log.d(
                "WifiTriangulation",
                """
                Position: $it
                Distances: ${distances.map { (ap, dist) ->
                    "${ap.ssid}: ${"%.2f".format(dist)}m"
                }}
                RSSI: ${rssiReadings.map { (bssid, rssi) ->
                    "${knownAccessPoints[bssid]?.ssid}: ${rssi}dBm"
                }}
                """.trimIndent()
            )
        }

        return position?.let { validatePosition(it) }
    }

    /**
     * Enhanced trilateration calculation
     */
    private fun calculatePosition(distances: Map<AccessPoint, Double>): Vector2? {
        if (distances.size < 3) {
            Log.v("ERR", "Insufficient measurements: ${distances.size}")
            return null
        }

        // Sort access points by distance (closer APs are more reliable)
        val sortedAPs = distances.entries.sortedBy { it.value }.map { it.key }
        val sortedDists = distances.entries.sortedBy { it.value }.map { it.value }

        // Create matrices with weighted measurements
        val n = sortedAPs.size
        val a = Array(n - 1) { DoubleArray(2) }
        val b = DoubleArray(n - 1)

        for (i in 0 until n - 1) {
            val weight = 1.0 / sortedDists[i] // Weight by inverse distance

            a[i][0] = 2 * (sortedAPs[i].xPos - sortedAPs[n - 1].xPos) * weight
            a[i][1] = 2 * (sortedAPs[i].yPos - sortedAPs[n - 1].yPos) * weight

            b[i] = (
                sortedDists[n - 1].pow(2) - sortedDists[i].pow(2) +
                    sortedAPs[i].xPos.pow(2) + sortedAPs[i].yPos.pow(2) -
                    sortedAPs[n - 1].xPos.pow(2) - sortedAPs[n - 1].yPos.pow(2)
                ) * weight
        }

        return try {
            calculateMatrixPosition(a, b)
        } catch (e: Exception) {
            Log.e("WifiTriangulation", "Position calculation failed", e)
            null
        }
    }

    private fun calculateMatrixPosition(
        a: Array<DoubleArray>,
        b: DoubleArray
    ): Vector2? {
        val n = a[0].size

        // Calculate AT (transpose of A)
        val at = Array(n) { DoubleArray(a.size) }
        for (i in 0 until n) {
            for (j in 0 until a.size) {
                at[i][j] = a[j][i]
            }
        }

        // Calculate ATA (AT × A)
        val ata = Array(n) { DoubleArray(n) }
        for (i in 0 until n) {
            for (j in 0 until n) {
                var sum = 0.0
                for (k in 0 until a.size) {
                    sum += at[i][k] * a[k][j]
                }
                ata[i][j] = sum
            }
        }

        // Calculate determinant
        val det = ata[0][0] * ata[1][1] - ata[0][1] * ata[1][0]
        if (det.absoluteValue < 1e-10) {
            return null // Singular matrix
        }

        // Calculate inverse of ATA
        val inv = Array(n) { DoubleArray(n) }
        inv[0][0] = ata[1][1] / det
        inv[0][1] = -ata[0][1] / det
        inv[1][0] = -ata[1][0] / det
        inv[1][1] = ata[0][0] / det

        // Calculate ATb
        val atb = DoubleArray(n)
        for (i in 0 until n) {
            var sum = 0.0
            for (j in 0 until a.size) {
                sum += at[i][j] * b[j]
            }
            atb[i] = sum
        }

        // Calculate final position
        val x = inv[0][0] * atb[0] + inv[0][1] * atb[1]
        val y = inv[1][0] * atb[0] + inv[1][1] * atb[1]

        return Vector2(x, y)
    }

    /**
     * Validate and adjust position if necessary
     */
    private fun validatePosition(position: Vector2): Vector2? {
        // Define your floor plan boundaries
        val minX = -5.0
        val maxX = 25.0
        val minY = -5.0
        val maxY = 15.0

        // Clamp position to valid range
        return Vector2(
            x = position.x.coerceIn(minX, maxX),
            y = position.y.coerceIn(minY, maxY)
        )
    }

    companion object {
        /**
         * Helper function to perform calibration measurements
         */
        fun startCalibration(
            context: Context,
            accessPoints: List<AccessPoint>,
            onComplete: (Map<String, Int>) -> Unit
        ) {
            val wifiManager =
                context.applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager

            val referenceRSSIs = mutableMapOf<String, MutableList<Int>>()
            var measurementCount = 0

            val handler = Handler(Looper.getMainLooper())
            val measurementRunnable =
                object : Runnable {
                    override fun run() {
                        if (measurementCount < 10) { // Take 10 measurements
                            val results = wifiManager.scanResults

                            accessPoints.forEach { ap ->
                                results.find { it.BSSID == ap.bssid }?.let { result ->
                                    referenceRSSIs
                                        .getOrPut(ap.bssid) { mutableListOf() }
                                        .add(result.level)
                                }
                            }

                            measurementCount++
                            handler.postDelayed(this, 1000) // Measure every second
                        } else {
                            // Calculate median values
                            val finalRSSIs =
                                referenceRSSIs.mapValues { (_, readings) ->
                                    readings.sorted().let { it[it.size / 2] }
                                }
                            onComplete(finalRSSIs)
                        }
                    }
                }

            handler.post(measurementRunnable)
        }
    }
}
