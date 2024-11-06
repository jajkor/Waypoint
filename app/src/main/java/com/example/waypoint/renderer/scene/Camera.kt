package com.example.waypoint.renderer.scene

import android.opengl.Matrix
import com.example.waypoint.renderer.Vector3
import kotlin.math.cos
import kotlin.math.sin

class Camera(
    private var position: Vector3 = Vector3(0.0f, 0.0f, 0.0f),
    private var pivot: Vector3 = Vector3(0.0f, 0.0f, 0.0f)
) {
    private var yaw: Float = 0f // Horizontal rotation angle
    private var pitch: Float = 89f // Vertical rotation angle
    private var distanceFromPivot: Float = 30f // Distance from pivot point (for zoom)

    // Limits for how close or far the camera can zoom
    private val minDistance: Float = 10f // Minimum zoom (closest)
    private val maxDistance: Float = 45f // Maximum zoom (farthest)

    private fun lookAt() {
        // Calculate the camera's position based on yaw, pitch, and distance from pivot
        val cameraX =
            pivot.x + distanceFromPivot * cos(Math.toRadians(yaw.toDouble())).toFloat() *
                cos(Math.toRadians(pitch.toDouble())).toFloat()
        val cameraY = pivot.y + distanceFromPivot * sin(Math.toRadians(pitch.toDouble())).toFloat()
        val cameraZ =
            pivot.z + distanceFromPivot * sin(Math.toRadians(yaw.toDouble())).toFloat() *
                cos(Math.toRadians(pitch.toDouble())).toFloat()

        // Update the camera position
        position = Vector3(cameraX, cameraY, cameraZ)
    }

    fun getPosition(): Vector3 = position

    fun setPivot(pivot: Vector3) {
        this.pivot = pivot
    }

    // Update zoom by adjusting the distance from the pivot point
    fun zoom(scaleFactor: Float) {
        // Increase or decrease distance, clamp between minDistance and maxDistance
        distanceFromPivot = minDistance.coerceAtLeast(maxDistance.coerceAtMost(distanceFromPivot / scaleFactor))
    }

    fun rotate(
        yawDelta: Float,
        pitchDelta: Float
    ) {
        yaw += yawDelta
        pitch = 20f.coerceAtLeast(89f.coerceAtMost(pitch + pitchDelta)) // Clamp pitch to avoid flipping
    }

    fun getViewMatrix(): FloatArray {
        val viewMatrix = FloatArray(16)
        lookAt() // Update the camera position based on yaw, pitch, and zoom
        // Create the view matrix looking at the pivot point
        Matrix.setLookAtM(
            viewMatrix,
            0,
            position.x,
            position.y,
            position.z,
            pivot.x,
            pivot.y,
            pivot.z,
            0f,
            1f,
            0f
        )
        return viewMatrix
    }
}
