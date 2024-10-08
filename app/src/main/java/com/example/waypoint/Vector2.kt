package com.example.waypoint

data class Vector2(var x: Float, var y: Float) {

    // Vector addition
    operator fun plus(other: Vector2): Vector2 {
        return Vector2(x + other.x, y + other.y)
    }

    // Vector subtraction
    operator fun minus(other: Vector2): Vector2 {
        return Vector2(x - other.x, y - other.y)
    }

    // Scalar multiplication
    operator fun times(scalar: Float): Vector2 {
        return Vector2(x * scalar, y * scalar)
    }

    // Scalar division
    operator fun div(scalar: Float): Vector2 {
        return Vector2(x / scalar, y / scalar)
    }

    // Dot product
    fun dot(other: Vector2): Float {
        return x * other.x + y * other.y
    }

    // Magnitude (length of the vector)
    fun magnitude(): Float {
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    // Normalization (make vector unit length)
    fun normalize(): Vector2 {
        val mag = magnitude()
        return if (mag != 0.0f) {
            Vector2(x / mag, y / mag)
        } else {
            this
        }
    }

    // Override toString for easier display of vector values
    override fun toString(): String {
        return "Vector2(x=$x, y=$y)"
    }
}
