package com.example.waypoint.location

import android.util.Log
import com.example.waypoint.Vector2
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt

class WifiTriangulation(
    data: LocationData,
) {
    private val knownAccessPoints: Map<String, AccessPoint>

    init {
        val locationData: LocationData = data

        // Create a map of BSSID to AccessPoint for easy lookup
        knownAccessPoints = locationData.accessPoints.associateBy { it.bssid }
    }

    /**
     * Calculate distance based on RSSI using the Log-distance path loss model
     * distance = 10^((|RSSI| - |referenceRssi|)/(10 * environmentalFactor))
     */
    private fun calculateDistance(
        rssi: Int,
        referenceRssi: Int,
        environmentalFactor: Double,
    ): Double = 10.0.pow((abs(rssi) - abs(referenceRssi)) / (10.0 * environmentalFactor))

    /**
     * Trilateration calculation using least squares method
     */
    private fun calculatePosition(distances: Map<AccessPoint, Double>): Vector2? {
        if (distances.size < 3) {
            Log.v("ERR", distances.size.toString())
            return null // Need at least 3 points for trilateration
        }

        // Convert to list for indexed access
        val aps = distances.keys.toList()
        val dists = distances.values.toList()

        // Create matrices for least squares calculation
        val n = aps.size
        val a = Array(n - 1) { DoubleArray(2) }
        val b = DoubleArray(n - 1)

        // Fill matrices
        for (i in 0 until n - 1) {
            a[i][0] = 2 * (aps[i].xPos - aps[n - 1].xPos)
            a[i][1] = 2 * (aps[i].yPos - aps[n - 1].yPos)

            b[i] = (dists[n - 1].pow(2) - dists[i].pow(2)) +
                (aps[i].xPos.pow(2) + aps[i].yPos.pow(2)) -
                (aps[n - 1].xPos.pow(2) + aps[n - 1].yPos.pow(2))
        }

        // Solve using pseudo-inverse (simplified for 2x2 matrix)
        val at = Array(2) { DoubleArray(n - 1) }
        for (i in 0 until 2) {
            for (j in 0 until n - 1) {
                at[i][j] = a[j][i]
            }
        }

        val ata = Array(2) { DoubleArray(2) }
        for (i in 0 until 2) {
            for (j in 0 until 2) {
                var sum = 0.0
                for (k in 0 until n - 1) {
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

        // Calculate inverse
        val inv = Array(2) { DoubleArray(2) }
        inv[0][0] = ata[1][1] / det
        inv[0][1] = -ata[0][1] / det
        inv[1][0] = -ata[1][0] / det
        inv[1][1] = ata[0][0] / det

        // Calculate AtB
        val atb = DoubleArray(2)
        for (i in 0 until 2) {
            var sum = 0.0
            for (j in 0 until n - 1) {
                sum += at[i][j] * b[j]
            }
            atb[i] = sum
        }

        // Final position calculation
        val x = inv[0][0] * atb[0] + inv[0][1] * atb[1]
        val y = inv[1][0] * atb[0] + inv[1][1] * atb[1]

        return Vector2(x, y)
    }

    /**
     * Process RSSI readings and estimate position
     */
    fun estimatePosition(rssiReadings: Map<String, Int>): Vector2? {
        val distances =
            rssiReadings
                .mapNotNull { (bssid, rssi) ->
                    knownAccessPoints[bssid]?.let { ap ->
                        ap to calculateDistance(rssi, ap.referenceRSSI, ap.environmentalFactor)
                    }
                }.toMap()

        return calculatePosition(distances)
    }

    /**
     * Calculate position error margin based on RSSI variance
     */
    fun calculateErrorMargin(rssiVariance: Double): Double {
        // Simple error estimation based on RSSI variance
        // You might want to adjust this based on your specific environment
        return sqrt(rssiVariance) * 0.5 // meters
    }
}
