package com.example.waypoint

import Vector3
import android.opengl.Matrix
import android.util.Log

class Camera(
    var position: Vector3 = Vector3(0.0f, 0.0f, 3.0f),
    var front: Vector3 = Vector3(0.0f, 0.0f, -1.0f),
    var up: Vector3 = Vector3(0.0f, 1.0f, 0.0f),
    var worldUp: Vector3 = Vector3(0.0f, 1.0f, 0.0f)
) {
    var yaw: Float = -90.0f
    var pitch: Float = 0.0f
    var movementSpeed: Float = 2.5f
    var zoomSpeed: Float = 0.1f
    var zoomLevel: Float = 45.0f  // Default field of view in degrees

    private val zoomMin = 1.0f
    private val zoomMax = 90.0f

    // Limits for panning (Optional)
    private val panXMin = -10.0f
    private val panXMax = 10.0f
    private val panYMin = -10.0f
    private val panYMax = 10.0f

    // Panning the camera by updating its position along the X and Y axes
    fun pan(offset: Vector2) {
        // Calculate right and up directions based on the current camera orientation
        val right = front.cross(up).normalize()

        // Calculate the new position by moving along the right (X) and up (Y) vectors
        position += right * offset.x
        position += up * offset.y

        // Optional: Clamp panning within the defined limits
        position.x = Math.max(panXMin, Math.min(panXMax, position.x))
        position.y = Math.max(panYMin, Math.min(panYMax, position.y))
    }

    // Zooming the camera by adjusting the field of view
    fun zoom(scaleFactor: Float) {
        zoomLevel /= scaleFactor  // Inverse scale to zoom in or out

        // Clamp the zoom level to avoid excessive zooming
        zoomLevel = Math.max(zoomMin, Math.min(zoomMax, zoomLevel))
    }

    // Get the view matrix for rendering (position and direction)
    fun getViewMatrix(): FloatArray {
        val lookAt = position + front
        return FloatArray(16).also {
            Matrix.setLookAtM(it, 0, position.x, position.y, position.z, lookAt.x, lookAt.y, lookAt.z, up.x, up.y, up.z)
        }
    }
}
