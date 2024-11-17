package com.example.waypoint

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.waypoint.location.WifiRssiScanner
import com.example.waypoint.renderer.MapGLSurfaceView

class MainActivity : AppCompatActivity() {
    private lateinit var gLView: MapGLSurfaceView
    private lateinit var wifiRssiScanner: WifiRssiScanner

    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Campus Data
        // val json = "campus/rydal_executive_plaza/third_floor/3rdfloor.json"
        // val locationData = parseJsonWithGson(this, json)

/*
        // Initialize WifiRssiScanner with fusion
        wifiRssiScanner =
            WifiRssiScanner(
                this,
                locationData,
                triangulation,
            ) { updatedRssiReadings ->
                if (updatedRssiReadings.size >= 3) {
                    val estimatedPosition = triangulation.estimatePosition(updatedRssiReadings)
                }
            }

 */

        gLView = MapGLSurfaceView(this)
        setContentView(gLView)
    }

    override fun onPause() {
        super.onPause()
        if (isScanning) {
            toggleScanning()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.v("SCAN", "SCANNING")
        toggleScanning()
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
}
