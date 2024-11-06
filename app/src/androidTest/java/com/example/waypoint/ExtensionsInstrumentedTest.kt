package com.example.waypoint

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExtensionsInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.waypoint", appContext.packageName)
    }

    @Test
    fun readRawText_isCorrect() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals(
            "Read from raw really works!",
            appContext.resources.readRawTextFile(R.raw.test_file),
        )
    }
}
