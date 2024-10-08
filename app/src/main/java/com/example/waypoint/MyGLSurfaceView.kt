package com.example.waypoint

import Vector3
import android.annotation.SuppressLint
import android.content.Context
import javax.microedition.khronos.egl.EGLConfig
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector

class MyGLSurfaceView(context: Context) : GLSurfaceView(context), GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {    private val renderer: MyGLRenderer
    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector
    private var camera = Camera()

    init {
        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8, 8, 8, 8, 16, 8); // Set the stencil size

        gestureDetector = GestureDetector(context, this)
        scaleGestureDetector = ScaleGestureDetector(context, this)

        renderer = MyGLRenderer(context, camera)
        setRenderer(renderer)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Pass the touch events to both detectors
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    // GestureDetector.OnGestureListener methods for handling swipe (pan)
    override fun onScroll(
        e1: MotionEvent?,
        p1: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        // Convert the scroll gesture to panning the camera
        val panSpeed = 0.01f  // Adjust pan speed if needed
        camera.pan(Vector2(-distanceX * panSpeed, distanceY * panSpeed))
        return true
    }

    // ScaleGestureDetector.OnScaleGestureListener methods for pinch-to-zoom
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val scaleFactor = detector.scaleFactor
        camera.zoom(scaleFactor)
        return true
    }

    // Unused methods from GestureDetector.OnGestureListener
    override fun onShowPress(p0: MotionEvent) {}
    override fun onSingleTapUp(p0: MotionEvent): Boolean = false
    override fun onDown(p0: MotionEvent): Boolean = true
    override fun onFling(
        e1: MotionEvent?,
        p1: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean = false
    override fun onLongPress(p0: MotionEvent) {}

    // Unused methods from ScaleGestureDetector.OnScaleGestureListener
    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true
    override fun onScaleEnd(detector: ScaleGestureDetector) {}
}