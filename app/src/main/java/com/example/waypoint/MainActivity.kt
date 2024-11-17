package com.example.waypoint

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.waypoint.location.AccessPoint
import com.example.waypoint.location.WifiScanner
import com.example.waypoint.renderer.MapGLSurfaceView
import com.example.waypoint.renderer.MapRenderer
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private var _mapGLSurfaceView: MapGLSurfaceView? = null
    val mapGLSurfaceView get() = _mapGLSurfaceView!!

    private var _toolbar: Toolbar? = null
    val toolbar get() = _toolbar!!

    private var _toggleButton: MaterialButton? = null
    val toggleButton get() = _toggleButton!!

    private var is3DMode = true
    private lateinit var renderer: MapRenderer

    private lateinit var wifiScanner: WifiScanner

    private val accessPoints = mutableListOf<AccessPoint>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        initializeMapGLSurfaceView()
        setupToolbar()
        setupToggleButton()

        // Initialize components
        wifiScanner = WifiScanner(this)
        loadAccessPoints()

        // Request permissions if needed
        if (!wifiScanner.hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE,
            )
        }

        // Start scanning and updating position
        lifecycleScope.launch {
            wifiScanner.scanWifi(accessPoints).collect { position ->
                // Update camera pivot and user model position
                renderer.updateCameraPivot(Vector3(position.x, 4.0f, position.y))
                Log.v("TRIANGULATION", "Updated position: $position")
            }
        }
    }

    private fun initializeViews() {
        _toolbar = findViewById(R.id.toolbar)
        _toggleButton = findViewById(R.id.btnToggleView)
    }

    private fun initializeMapGLSurfaceView() {
        // Create the MapGLSurfaceView
        _mapGLSurfaceView = MapGLSurfaceView(this)
        renderer = mapGLSurfaceView.getRenderer()

        // Get the container and add the MapGLSurfaceView to it
        val container = findViewById<FrameLayout>(R.id.glContainer)
        container.addView(mapGLSurfaceView)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupToggleButton() {
        toggleButton.text = if (is3DMode) "2D View" else "3D View"

        toggleButton.setOnClickListener {
            is3DMode = !is3DMode
            toggleButton.text = if (is3DMode) "2D View" else "3D View"
            updateViewMode(is3DMode)
        }
    }

    private fun updateViewMode(is3D: Boolean) {
        if (!is3D) {
            // 2D Mode: Lock pitch to 89 degrees (top-down view)
            renderer.setPitch(89f)
            renderer.setFreeLook(false)
        } else {
            // 3D Mode: Enable free camera movement
            renderer.setFreeLook(true)
        }

        mapGLSurfaceView.requestRender()
    }

    override fun onResume() {
        super.onResume()
        _mapGLSurfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        _mapGLSurfaceView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up references
        val container = findViewById<FrameLayout>(R.id.glContainer)
        container.removeAllViews()
        _mapGLSurfaceView = null
        _toolbar = null
        _toggleButton = null
    }

    private fun loadAccessPoints() {
        val json = assets.open("campus/rydal_executive_plaza/third_floor/3rdfloor.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(json)
        val apArray = jsonObject.getJSONArray("access-points")

        for (i in 0 until apArray.length()) {
            val apJson = apArray.getJSONObject(i)
            accessPoints.add(AccessPoint.fromJson(apJson.toMap()))
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}
