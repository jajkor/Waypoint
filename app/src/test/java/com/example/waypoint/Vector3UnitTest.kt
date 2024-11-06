package com.example.waypoint

import com.example.waypoint.renderer.Vector3
import org.junit.Assert.assertEquals
import org.junit.Test

class Vector3UnitTest {
    @Test
    fun plus_isCorrect() {
        assertEquals(Vector3(2.0f, 2.0f, 2.0f), Vector3(1.0f, 1.0f, 1.0f) + Vector3(1.0f, 1.0f, 1.0f))
    }

    @Test
    fun minus_isCorrect() {
        assertEquals(Vector3(1.0f, 1.0f, 1.0f), Vector3(2.0f, 2.0f, 2.0f) - Vector3(1.0f, 1.0f, 1.0f))
    }

    @Test
    fun times_isCorrect() {
        assertEquals(Vector3(4.0f, 4.0f, 4.0f), Vector3(2.0f, 2.0f, 2.0f) * 2.0f)
    }

    @Test
    fun div_isCorrect() {
        assertEquals(Vector3(2.0f, 2.0f, 2.0f), Vector3(4.0f, 4.0f, 4.0f) / 2.0f)
    }

    @Test
    fun divByZero_isCorrect() {
        assertEquals(Vector3(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), Vector3(4.0f, 4.0f, 4.0f) / 0f)
    }

    @Test
    fun tostring_isCorrect() {
        assertEquals("Vector3(x=4.0, y=4.0, z=4.0)", Vector3(4.0f, 4.0f, 4.0f).toString())
    }
}
