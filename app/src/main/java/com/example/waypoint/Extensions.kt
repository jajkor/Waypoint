package com.example.waypoint

import android.content.res.Resources
import androidx.annotation.RawRes
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

fun FloatArray.toFloatBuffer(): FloatBuffer =
    ByteBuffer
        .allocateDirect(this.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .also {
            it.put(this).position(0)
        }

fun Resources.readRawTextFile(
    @RawRes id: Int,
) = openRawResource(id).bufferedReader().use { it.readText() }

fun IntArray.toIntBuffer(): IntBuffer =
    ByteBuffer
        .allocateDirect(this.size * Int.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asIntBuffer()
        .also {
            it.put(this).position(0)
        }
