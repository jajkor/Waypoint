package com.example.waypoint

import android.content.Context
import android.icu.number.Scale
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyGLRenderer
    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector

    init {
        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(3)

        setEGLConfigChooser(8, 8, 8, 8, 16, 8); // Set the stencil size

        // Set the Renderer for drawing on the GLSurfaceView
        renderer = MyGLRenderer(context)
        setRenderer(renderer)
        //renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        gestureDetector = GestureDetector(context, GestureListener())
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            renderer.onScroll(distanceX, distanceY)
            return true
        }
    }

    private inner class ScaleListener() : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector) : Boolean {
            renderer.onScale(detector.scaleFactor)
            return true
        }
    }
}
