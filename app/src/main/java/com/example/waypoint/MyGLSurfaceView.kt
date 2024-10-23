package com.example.waypoint

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector

class MyGLSurfaceView(
    context: Context,
) : GLSurfaceView(context) {
    private val renderer: MyGLRenderer
    private var camera = Camera3D()
    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector

    init {
        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(3)

        // By default GLSurfaceView chooses a EGLConfig that has an RGB_888 pixel format, with at least a 16-bit depth buffer and no stencil.
        setEGLConfigChooser(8, 8, 8, 8, 16, 8)

        // Set the Renderer for drawing on the GLSurfaceView
        renderer = MyGLRenderer(context, camera)
        setRenderer(renderer)

        gestureDetector = GestureDetector(context, GestureListener())
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Pass the event to both the scale and gesture detectors
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    // Inner class for handling zoom (pinch gestures)
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            camera.zoom(scaleFactor) // Adjust camera zoom
            requestRender() // Request a redraw with the updated zoom
            return true
        }
    }

    // Inner class for handling swipe to rotate the camera around the pivot (panning)
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private val swipeThreshold = 0.1f // Sensitivity for rotation based on swipe distance

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float,
        ): Boolean {
            // Convert touch movement into camera rotation
            val deltaYaw = distanceX * swipeThreshold
            val deltaPitch = distanceY * swipeThreshold
            camera.rotate(-deltaYaw, -deltaPitch) // Invert to get natural swipe behavior
            requestRender() // Redraw the scene with updated camera rotation
            return true
        }
    }
}
