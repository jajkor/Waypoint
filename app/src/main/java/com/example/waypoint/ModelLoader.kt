package com.example.waypoint

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class ModelLoader(
    private val context: Context,
) {
    fun loadModel(objFileName: String): Model {
        val inputStream = context.assets.open(objFileName)
        val reader = BufferedReader(InputStreamReader(inputStream))

        val vertices = mutableListOf<Float>()
        val texCoords = mutableListOf<Float>()
        val indices = mutableListOf<Int>()

        reader.use { r ->
            r.forEachLine { line ->
                val parts = line.split(" ")
                when (parts[0]) {
                    "v" -> vertices.addAll(parts.subList(1, 4).map { it.toFloat() })
                    "vt" -> texCoords.addAll(parts.subList(1, 3).map { it.toFloat() })
                    "f" ->
                        parts.subList(1, 4).forEach { face ->
                            val vertexData = face.split("/")
                            indices.add(vertexData[0].toInt() - 1)
                        }
                }
            }
        }

        val normals = calculateNormals(vertices, indices)
        return Model(vertices, normals, texCoords, indices)
    }

    private fun calculateFaceNormal(
        v0: Vector3,
        v1: Vector3,
        v2: Vector3,
    ): Vector3 {
        val edge1 = v1 - v0
        val edge2 = v2 - v0
        return edge1.cross(edge2).normalize()
    }

    private fun calculateNormals(
        vertices: List<Float>,
        indices: List<Int>,
    ): List<Float> {
        val vertexCount = vertices.size / 3
        val normalAccumulator = MutableList(vertexCount) { Vector3(0f, 0f, 0f) }

        // Calculate and accumulate face normals for each vertex
        for (i in indices.indices step 3) {
            val i0 = indices[i]
            val i1 = indices[i + 1]
            val i2 = indices[i + 2]

            val v0 = Vector3.fromList(vertices.subList(i0 * 3, i0 * 3 + 3))
            val v1 = Vector3.fromList(vertices.subList(i1 * 3, i1 * 3 + 3))
            val v2 = Vector3.fromList(vertices.subList(i2 * 3, i2 * 3 + 3))

            val normal = calculateFaceNormal(v0, v1, v2)

            // Accumulate normals for each vertex
            normalAccumulator[i0] = normalAccumulator[i0] + normal
            normalAccumulator[i1] = normalAccumulator[i1] + normal
            normalAccumulator[i2] = normalAccumulator[i2] + normal
        }

        // Normalize accumulated normals and convert to flat list
        return normalAccumulator
            .map { it.normalize() }
            .flatMap { it.toList() }
    }
}
