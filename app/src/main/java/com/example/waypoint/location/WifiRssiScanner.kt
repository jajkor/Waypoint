package com.example.waypoint.location

import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class WifiRssiScanner(
    private val context: Context,
    private val data: LocationData, // Path to the JSON file
    private val onRssiUpdate: (Map<String, Int>) -> Unit,
) {
    private val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    // List of BSSIDs dynamically populated from the JSON file
    private val targetAccessPoints: List<String>

    // Scanning interval in milliseconds
    private val scanInterval = 500L

    private val handler = Handler(Looper.getMainLooper())
    private val scanRunnable =
        object : Runnable {
            override fun run() {
                performWiFiScan()
                handler.postDelayed(this, scanInterval)
            }
        }

    init {
        val locationData: LocationData = data

        targetAccessPoints = locationData.accessPoints.map { it.bssid }
    }

    fun startRssiScanning() {
        // Check and request necessary permissions
        if (checkAndRequestPermissions()) {
            handler.post(scanRunnable)
        }
    }

    fun stopRssiScanning() {
        handler.removeCallbacks(scanRunnable)
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissionsToRequest = mutableListOf<String>()

        // Location permissions (required for WiFi scanning)
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Nearby devices permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.NEARBY_WIFI_DEVICES,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }

        // Request permissions if needed
        if (permissionsToRequest.isNotEmpty()) {
            if (context is AppCompatActivity) {
                ActivityCompat.requestPermissions(
                    context,
                    permissionsToRequest.toTypedArray(),
                    PERMISSION_REQUEST_CODE,
                )
                return false
            }
            return false
        }

        return true
    }

    private fun performWiFiScan() {
        // Ensure permissions are granted before scanning
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Trigger WiFi scan
        val success = wifiManager.startScan()

        if (success) {
            // Get scan results
            val scanResults = wifiManager.scanResults

            // Filter and process target access points
            val targetResults = scanResults.filter { it.BSSID in targetAccessPoints }

            // Create a map of BSSID to RSSI readings
            val rssiReadings = targetResults.associate { it.BSSID to it.level }
            Log.v("RSSI", rssiReadings.toString())
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 1001
    }
}
