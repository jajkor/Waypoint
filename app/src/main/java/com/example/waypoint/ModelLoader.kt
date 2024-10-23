package com.example.waypoint

import android.content.Context
import de.javagl.obj.ObjData
import de.javagl.obj.ObjReader
import de.javagl.obj.ObjUtils
import java.io.File

class ModelLoader(
    private val context: Context,
) {
    fun loadModel(objFileName: String): Model {
        val obj =
            context.assets.open(File(objFileName).path).use {
                ObjReader.read(it)
            }

        val renderableObj = ObjUtils.convertToRenderable(obj)

        return Model(
            ObjData.getVertices(renderableObj),
            ObjData.getNormals(renderableObj),
            ObjData.getTexCoords(renderableObj, 2),
            ObjData.getFaceVertexIndices(renderableObj),
        )
    }
}
