package com.example.waypoint.renderer.scene

import android.os.SystemClock

class Timer {
    private val startTimeMillis: Long

    private var lastFrameMillis: Long

    init {
        SystemClock.elapsedRealtime().also {
            lastFrameMillis = it
            startTimeMillis = it
        }
    }

    fun sinceLastFrameSecs() = (SystemClock.elapsedRealtime() - lastFrameMillis) / 1000.0f
}
