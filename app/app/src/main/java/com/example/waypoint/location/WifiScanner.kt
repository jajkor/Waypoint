package com.example.waypoint.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.core.app.ActivityCompat
import com.example.waypoint.Vector2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.abs
import kotlin.math.pow

class WifiScanner(
    private val context: Context,
) {
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun hasRequiredPermissions(): Boolean =
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_WIFI_STATE,
            ) == PackageManager.PERMISSION_GRANTED

    @Suppress("MissingPermission")
    fun scanWifi(knownAccessPoints: List<AccessPoint>): Flow<Vector2> =
        flow {
            if (!hasRequiredPermissions()) {
                throw SecurityException("Required permissions not granted")
            }

            while (true) {
                // Start a scan
                wifiManager.startScan()

                // Get scan results with explicit permission check
                val scanResults =
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        wifiManager.scanResults
                    } else {
                        throw SecurityException("Location permission not granted")
                    }

                val readings = mutableMapOf<String, Int>()

                // Match scan results with known access points
                for (result in scanResults) {
                    knownAccessPoints.find { it.bssid == result.BSSID }?.let {
                        readings[it.bssid] = result.level
                    }
                }

                if (readings.size >= 3) {
                    val position = calculatePosition(readings, knownAccessPoints)
                    emit(position)
                }

                kotlinx.coroutines.delay(1000) // Scan every second
            }
        }

    private fun calculatePosition(
        readings: Map<String, Int>,
        knownAccessPoints: List<AccessPoint>,
    ): Vector2 {
        val distances =
            readings.map { (bssid, rssi) ->
                val ap = knownAccessPoints.first { it.bssid == bssid }
                val distance = calculateDistance(rssi, ap.referenceRSSI, ap.environmentalFactor)
                Circle(ap.position, distance)
            }

        return trilaterate(distances)
    }

    private fun calculateDistance(
        rssi: Int,
        referenceRSSI: Int,
        environmentalFactor: Double,
    ): Double = 10.0.pow((abs(referenceRSSI) - abs(rssi)) / (10 * environmentalFactor))
}

fun trilaterate(circles: List<Circle>): Vector2 {
    if (circles.size < 3) throw IllegalArgumentException("Need at least 3 circles for trilateration")

    // Using first three circles for triangulation
    val (p1, p2, p3) = circles.take(3).map { it.center }
    val (r1, r2, r3) = circles.take(3).map { it.radius }

    // Implementation of trilateration algorithm
    val a = 2 * (p2.x - p1.x)
    val b = 2 * (p2.y - p1.y)
    val c = r1 * r1 - r2 * r2 - p1.x * p1.x + p2.x * p2.x - p1.y * p1.y + p2.y * p2.y
    val d = 2 * (p3.x - p2.x)
    val e = 2 * (p3.y - p2.y)
    val f = r2 * r2 - r3 * r3 - p2.x * p2.x + p3.x * p3.x - p2.y * p2.y + p3.y * p3.y

    val x = (c * e - f * b) / (e * a - b * d)
    val y = (c * d - a * f) / (b * d - a * e)

    return Vector2(x.toFloat(), y.toFloat())
}
