package com.example.waypoint.model

import Vector3
import android.content.Context
import android.graphics.BitmapFactory
import com.example.waypoint.Texture
import de.javagl.obj.FloatTuple
import de.javagl.obj.MtlReader
import de.javagl.obj.ObjData
import de.javagl.obj.ObjReader
import de.javagl.obj.ObjSplitting
import de.javagl.obj.ObjUtils
import java.io.File

object ObjLoader {

    fun fromAssets(
        context: Context,
        directory: String,
        objFileName: String,
        mtlFileName: String,
        specularTextureFileName: String? = null
    ): Model {
        val obj = context.assets.open(File(directory, objFileName).path).use {
            ObjReader.read(it)
        }
        val materials = context.assets.open(File(directory, mtlFileName).path).use {
            MtlReader.read(it)
        }

        val meshes = mutableListOf<Mesh>()
        val textures = mutableMapOf<String, Texture>()

        ObjSplitting.splitByMaterialGroups(obj).forEach { (materialName, group) ->
            val material = materials.first { it.name == materialName }
            val renderableObj = ObjUtils.convertToRenderable(group)
            val diffuseTexture =
                material.mapKd?.let { createTexture(context, File(directory, it), textures) }
            val specularTexture = specularTextureFileName?.let {
                createTexture(
                    context,
                    File(directory, it),
                    textures
                )
            }

            val mesh = Mesh(
                vertices = ObjData.getVertices(renderableObj),
                normals = ObjData.getNormals(renderableObj),
                texCoords = ObjData.getTexCoords(renderableObj, 2),
                indices = ObjData.getFaceVertexIndices(renderableObj),
                material = Material(
                    ambientColor = material.ka.toVector3(),
                    diffuseColor = material.kd.toVector3(),
                    specularColor = material.ks.toVector3(),
                    diffuseTexture = diffuseTexture,
                    specularTexture = specularTexture
                )
            )
            meshes.add(mesh)
        }

        return Model(meshes)
    }

    private fun createTexture(
        context: Context,
        file: File,
        knownTextures: MutableMap<String, Texture>
    ) = knownTextures[file.path] ?: run {
        context.assets.open(file.path).use {
            BitmapFactory.decodeStream(
                it,
                null,
                BitmapFactory.Options().apply { inScaled = false })?.let { bitmap ->
                Texture(bitmap).also { texture ->
                    knownTextures[file.path] = texture
                }
            }
        }
    }
}

private fun FloatTuple.toVector3() = Vector3(x, y, z)
