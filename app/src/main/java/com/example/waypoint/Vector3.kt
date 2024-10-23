package com.example.waypoint

data class Vector3(
    var x: Float,
    var y: Float,
    var z: Float,
) {
    // Vector addition
    operator fun plus(other: Vector3): Vector3 = Vector3(x + other.x, y + other.y, z + other.z)

    // Vector subtraction
    operator fun minus(other: Vector3): Vector3 = Vector3(x - other.x, y - other.y, z - other.z)

    // Scalar multiplication
    operator fun times(scalar: Float): Vector3 = Vector3(x * scalar, y * scalar, z * scalar)

    // Scalar division
    operator fun div(scalar: Float): Vector3 = Vector3(x / scalar, y / scalar, z / scalar)

    // Override toString for easier display of vector values
    override fun toString(): String = "Vector3(x=$x, y=$y, z=$z)"
}
