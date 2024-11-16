package com.example.waypoint.location

import com.example.waypoint.Vector2

class KalmanFilter {
    // State vector: [x, y, vx, vy]
    private var state = FloatArray(4) { 0f }
    private var covariance = Array(4) { FloatArray(4) { if (it == it) 100f else 0f } }

    // System matrices
    private val dt = 0.1f // Time step
    private val processNoise = 0.1f
    private val measurementNoise = 1.0f

    fun predict() {
        // State prediction
        state[0] += state[2] * dt // x += vx * dt
        state[1] += state[3] * dt // y += vy * dt

        // Update covariance
        covariance = updateCovariance(covariance)
    }

    fun update(measurement: Vector2) {
        // Kalman gain calculation
        val k = calculateKalmanGain()

        // Update state
        val innovation = FloatArray(2)
        innovation[0] = measurement.x.toFloat() - state[0]
        innovation[1] = measurement.y.toFloat() - state[1]

        state[0] += k[0][0] * innovation[0] + k[0][1] * innovation[1]
        state[1] += k[1][0] * innovation[0] + k[1][1] * innovation[1]
        state[2] += k[2][0] * innovation[0] + k[2][1] * innovation[1]
        state[3] += k[3][0] * innovation[0] + k[3][1] * innovation[1]

        // Update covariance
        updateCovarianceWithMeasurement(k)
    }

    fun getPosition(): Vector2 = Vector2(state[0].toDouble(), state[1].toDouble())

    private fun updateCovariance(cov: Array<FloatArray>): Array<FloatArray> {
        // Simplified covariance prediction
        val q = processNoise * processNoise
        cov[0][0] += dt * dt * q
        cov[1][1] += dt * dt * q
        cov[2][2] += q
        cov[3][3] += q
        return cov
    }

    private fun calculateKalmanGain(): Array<FloatArray> {
        // Simplified Kalman gain calculation
        val r = measurementNoise * measurementNoise
        val s = covariance[0][0] + r
        val k = Array(4) { FloatArray(2) }
        k[0][0] = covariance[0][0] / s
        k[1][1] = covariance[1][1] / s
        return k
    }

    private fun updateCovarianceWithMeasurement(k: Array<FloatArray>) {
        // Simplified covariance update
        covariance[0][0] *= (1 - k[0][0])
        covariance[1][1] *= (1 - k[1][1])
    }
}
