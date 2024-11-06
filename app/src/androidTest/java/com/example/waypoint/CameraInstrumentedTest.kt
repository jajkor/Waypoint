package com.example.waypoint

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.waypoint.renderer.scene.Camera
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class CameraInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.waypoint", appContext.packageName)
    }

    @Test
    fun rotateUpper_isCorrect() {
        val camera = Camera()

        val expectedMatrix = camera.getViewMatrix()
        camera.rotate(0f, 89f)

        val actualMatrix = camera.getViewMatrix()
        camera.rotate(0f, 180f)

        assertEquals(true, expectedMatrix.contentEquals(actualMatrix))
    }

    @Test
    fun rotateLower_isCorrect() {
        val camera = Camera()

        val expectedMatrix = camera.getViewMatrix()
        camera.rotate(0f, 20f)

        val actualMatrix = camera.getViewMatrix()
        camera.rotate(0f, -180f)

        assertEquals(true, expectedMatrix.contentEquals(actualMatrix))
    }
}
