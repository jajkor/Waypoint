package com.example.waypoint

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class ModelLoader(private val context: Context) {
    fun loadModel(fileName: String): ObjModel {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))

        val vertices = mutableListOf<Float>()
        val normals = mutableListOf<Float>()
        val texCoords = mutableListOf<Float>()
        val indices = mutableListOf<Int>()

        reader.forEachLine { line ->
            val parts = line.split(" ")
            when (parts[0]) {
                "v" -> {  // Vertex coordinates
                    vertices.addAll(parts.subList(1, 4).map { it.toFloat() })
                }
                "vt" -> {  // Texture coordinates
                    texCoords.addAll(parts.subList(1, 3).map { it.toFloat() })
                }
                "vn" -> {  // Normals
                    normals.addAll(parts.subList(1, 4).map { it.toFloat() })
                }
                "f" -> {  // Faces (indices)
                    parts.subList(1, 4).forEach { face ->
                        val vertexData = face.split("/")
                        indices.add(vertexData[0].toInt() - 1)  // OBJ indexing starts from 1
                    }
                }
            }
        }

        return ObjModel(vertices, texCoords, normals, indices)
    }
}
