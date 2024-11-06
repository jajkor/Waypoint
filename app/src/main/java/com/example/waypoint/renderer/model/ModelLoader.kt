package com.example.waypoint.renderer.model

import android.content.Context
import com.example.waypoint.renderer.Vector3
import de.javagl.obj.FloatTuple
import de.javagl.obj.MtlReader
import de.javagl.obj.ObjData
import de.javagl.obj.ObjReader
import de.javagl.obj.ObjUtils
import java.io.File

class ModelLoader(
    private val context: Context
) {
    fun loadModel(
        objFileName: String,
        mtlFileName: String
    ): Model {
        val obj =
            context.assets.open(File(objFileName).path).use {
                ObjReader.read(it)
            }

        val mtl =
            context.assets.open(File(mtlFileName).path).use {
                MtlReader.read(it)
            }

        val material = mtl.first()
        val renderableObj = ObjUtils.convertToRenderable(obj)

        return Model(
            ObjData.getVertices(renderableObj),
            ObjData.getNormals(renderableObj),
            ObjData.getTexCoords(renderableObj, 2),
            ObjData.getFaceVertexIndices(renderableObj),
            Material(
                material.ka.toVector3(),
                material.kd.toVector3(),
                material.ks.toVector3(),
                material.ns
            )
        )
    }
}

private fun FloatTuple.toVector3() = Vector3(x, y, z)
