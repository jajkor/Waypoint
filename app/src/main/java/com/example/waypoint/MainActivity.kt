package com.example.waypoint

import WifiRssiScanner
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.waypoint.location.WifiTriangulation
import com.example.waypoint.location.parseJsonWithGson
import com.example.waypoint.renderer.MapGLSurfaceView

class MainActivity : AppCompatActivity() {
    private lateinit var gLView: MapGLSurfaceView
    private lateinit var wifiRssiScanner: WifiRssiScanner
    private lateinit var triangulation: WifiTriangulation

    private var isScanning = false

    // RSSI readings from scanned WiFi networks
    private val rssiReadings = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Campus Data
        val json = "campus/rydal_executive_plaza/third_floor/3rdfloor.json"
        val obj = "campus/rydal_executive_plaza/third_floor/3rdfloor.obj"
        val mtl = "campus/rydal_executive_plaza/third_floor/3rdfloor.mtl"

        val locationData = parseJsonWithGson(this, json)

        // Initialize WifiTriangulation with the JSON file
        triangulation = WifiTriangulation(locationData)
        // Initialize WifiRssiScanner with callback for RSSI updates
        wifiRssiScanner =
            WifiRssiScanner(
                this,
                locationData,
                triangulation,
            ) { updatedRssiReadings ->
                rssiReadings.putAll(updatedRssiReadings)

                // Calculate position when enough readings are available
                if (rssiReadings.size >= 3) {
                    val estimatedPosition = triangulation.estimatePosition(rssiReadings)
                    if (estimatedPosition != null) {
                        // Update the GLSurfaceView with the estimated position
                        gLView.updatePosition(estimatedPosition)
                    }
                }
            }

        // Create a GLSurfaceView instance and set it as the content view
        gLView = MapGLSurfaceView(this)
        setContentView(gLView)
    }

    private fun toggleScanning() {
        if (!isScanning) {
            wifiRssiScanner.startRssiScanning()
            isScanning = true
        } else {
            wifiRssiScanner.stopRssiScanning()
            isScanning = false
        }
    }

    override fun onStart() {
        super.onStart()
        Log.v("SCAN", "SCANNING")
        // Start RSSI scanning
        toggleScanning()
    }

    override fun onPause() {
        super.onPause()
        Log.v("SCAN", "NOT SCANNING")

        // Stop RSSI scanning to conserve resources
        toggleScanning()
    }
}
