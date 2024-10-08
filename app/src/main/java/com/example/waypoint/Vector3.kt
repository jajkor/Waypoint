package com.example.waypoint

data class Vector3(var x: Float, var y: Float, var z: Float) {

    // Vector addition
    operator fun plus(other: Vector3): Vector3 {
        return Vector3(x + other.x, y + other.y, z + other.z)
    }

    // Vector subtraction
    operator fun minus(other: Vector3): Vector3 {
        return Vector3(x - other.x, y - other.y, z - other.z)
    }

    // Scalar multiplication
    operator fun times(scalar: Float): Vector3 {
        return Vector3(x * scalar, y * scalar, z * scalar)
    }

    // Scalar division
    operator fun div(scalar: Float): Vector3 {
        return Vector3(x / scalar, y / scalar, z / scalar)
    }

    // Dot product
    fun dot(other: Vector3): Float {
        return x * other.x + y * other.y + z * other.z
    }

    // Cross product
    fun cross(other: Vector3): Vector3 {
        return Vector3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    // Magnitude (length of the vector)
    fun magnitude(): Float {
        return Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
    }

    // Normalization (make vector unit length)
    fun normalize(): Vector3 {
        val mag = magnitude()
        return if (mag != 0.0f) {
            Vector3(x / mag, y / mag, z / mag)
        } else {
            this
        }
    }

    // Override toString for easier display of vector values
    override fun toString(): String {
        return "Vector3(x=$x, y=$y, z=$z)"
    }
}