package com.example.waypoint

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer


// Extension function to convert lists to buffers
fun List<Float>.toFloatBuffer(): FloatBuffer {
    val buffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
    buffer.put(this.toFloatArray()).position(0)
    return buffer
}

fun List<Int>.toIntBuffer(): IntBuffer {
    val buffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asIntBuffer()
    buffer.put(this.toIntArray()).position(0)
    return buffer
}