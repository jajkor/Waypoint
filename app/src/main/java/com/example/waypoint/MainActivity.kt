package com.example.waypoint

import WifiRssiScanner
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.waypoint.location.AccessPoint
import com.example.waypoint.location.CalibrationManager
import com.example.waypoint.location.SensorFusionManager
import com.example.waypoint.location.WifiTriangulation
import com.example.waypoint.location.parseJsonWithGson
import com.example.waypoint.renderer.MapGLSurfaceView

class MainActivity : AppCompatActivity() {
    private lateinit var gLView: MapGLSurfaceView
    private lateinit var wifiRssiScanner: WifiRssiScanner
    private lateinit var triangulation: WifiTriangulation
    private lateinit var calibrationManager: CalibrationManager
    private lateinit var sensorFusionManager: SensorFusionManager

    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SensorFusionManager
        sensorFusionManager =
            SensorFusionManager(this) { position ->
                gLView.updatePosition(position)
            }

        // Campus Data
        val json = "campus/rydal_executive_plaza/third_floor/3rdfloor.json"
        val locationData = parseJsonWithGson(this, json)

        calibrationManager = CalibrationManager(this)
        triangulation = WifiTriangulation(this, locationData)

        // Check if calibration is needed
        if (triangulation.needsCalibration()) {
            showCalibrationDialog()
        }

        // Initialize WifiRssiScanner with fusion
        wifiRssiScanner =
            WifiRssiScanner(
                this,
                locationData,
                triangulation
            ) { updatedRssiReadings ->
                if (updatedRssiReadings.size >= 3) {
                    val estimatedPosition = triangulation.estimatePosition(updatedRssiReadings)
                    if (estimatedPosition != null) {
                        // Update sensor fusion with WiFi position
                        sensorFusionManager.processWifiPosition(estimatedPosition)
                    }
                }
            }

        gLView = MapGLSurfaceView(this)
        setContentView(gLView)

        // Add calibration menu option
        addCalibrationMenuItem()
    }

    override fun onResume() {
        super.onResume()
        sensorFusionManager.registerSensors()
    }

    override fun onPause() {
        super.onPause()
        sensorFusionManager.unregisterSensors()
        if (isScanning) {
            toggleScanning()
        }
    }

    private fun addCalibrationMenuItem() {
        gLView.setOnCreateContextMenuListener { menu, _, _ ->
            menu.add(Menu.NONE, 1, Menu.NONE, "Recalibrate").setOnMenuItemClickListener {
                startCalibration()
                true
            }
        }
    }

    private fun showCalibrationDialog() {
        AlertDialog
            .Builder(this)
            .setTitle("Calibration Required")
            .setMessage("The system needs to be calibrated for accurate positioning. Would you like to calibrate now?")
            .setPositiveButton("Yes") { _, _ ->
                startCalibration()
            }.setNegativeButton("Later") { dialog, _ ->
                dialog.dismiss()
            }.setCancelable(false) // Force user to make a choice
            .show()
    }

    private fun startCalibration() {
        val locationData = parseJsonWithGson(this, "campus/rydal_executive_plaza/third_floor/3rdfloor.json")
        calibrateNextAP(locationData.accessPoints, 0, 0)
    }

    private fun calibrateNextAP(
        accessPoints: List<AccessPoint>,
        apIndex: Int,
        distanceIndex: Int
    ) {
        if (apIndex >= accessPoints.size) {
            // All APs calibrated
            Toast.makeText(this, "Calibration complete!", Toast.LENGTH_SHORT).show()
            return
        }

        val ap = accessPoints[apIndex]
        val distances = listOf(1.0, 3.0, 5.0)

        if (distanceIndex >= distances.size) {
            // Move to next AP
            calibrateNextAP(accessPoints, apIndex + 1, 0)
            return
        }

        val currentDistance = distances[distanceIndex]

        // Show instructions dialog
        AlertDialog
            .Builder(this)
            .setTitle("Calibrating ${ap.ssid}")
            .setMessage(
                """
                Please follow these steps:
                1. Stand exactly ${currentDistance}m from ${ap.ssid}
                2. Stay still during the measurement
                3. Press 'Start' when ready
                
                Progress: AP ${apIndex + 1}/${accessPoints.size}
                Distance: ${currentDistance}m
                """.trimIndent()
            ).setPositiveButton("Start") { _, _ ->
                showCalibrationProgress(ap, currentDistance) { success ->
                    if (success) {
                        // Move to next distance or AP
                        calibrateNextAP(accessPoints, apIndex, distanceIndex + 1)
                    } else {
                        // Retry current measurement
                        calibrateNextAP(accessPoints, apIndex, distanceIndex)
                    }
                }
            }.setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show()
    }

    private fun showCalibrationProgress(
        ap: AccessPoint,
        distance: Double,
        onComplete: (Boolean) -> Unit
    ) {
        val progressDialog =
            ProgressDialog(this).apply {
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                setTitle("Calibrating ${ap.ssid}")
                setMessage("Measuring at ${distance}m")
                setCancelable(false)
                max = 100
                show()
            }

        calibrationManager.startCalibration(
            accessPoint = ap,
            currentDistance = distance,
            onProgress = { progress, message ->
                progressDialog.progress = progress
                progressDialog.setMessage(message)
            },
            onDistanceComplete = {
                progressDialog.dismiss()
                onComplete(true)
            },
            onError = { error ->
                progressDialog.dismiss()
                AlertDialog
                    .Builder(this)
                    .setTitle("Calibration Error")
                    .setMessage("Error: $error\nWould you like to retry?")
                    .setPositiveButton("Retry") { _, _ -> onComplete(false) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
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
