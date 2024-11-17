package com.example.waypoint

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.waypoint.renderer.model.ModelLoader
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModelLoaderInstrumentedTest {
    @Test
    fun loadModelMaterial_isCorrect() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val model = ModelLoader(appContext).loadModel("models/test/stanford-bunny.obj", "models/test/stanford-bunny.mtl")

        assertEquals(Vector3(1.000f, 0.000f, 0.000f), model.getMaterial().ambientColor)
        assertEquals(Vector3(0.000f, 1.000f, 0.000f), model.getMaterial().diffuseColor)
        assertEquals(Vector3(0.000f, 0.000f, 1.000f), model.getMaterial().specularColor)
    }
}
