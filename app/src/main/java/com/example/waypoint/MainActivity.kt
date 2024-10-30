package com.example.waypoint

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import com.example.waypoint.renderer.MapGLSurfaceView

class MainActivity : Activity() {
    private lateinit var gLView: GLSurfaceView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        gLView = MapGLSurfaceView(this)
        setContentView(gLView)
    }
}
