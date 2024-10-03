package com.example.waypoint

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class ModelLoader(private val context: Context) {
    fun loadModel(fileName: String): ObjModel {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))

        val vertices = mutableListOf<Float>()
        val texCoords = mutableListOf<Float>()
        val normals = mutableListOf<Float>()
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
                    //normals.addAll(parts.subList(1, 4).map { it.toFloat() })
                }
                "f" -> {  // Faces (indices)
                    parts.subList(1, 4).forEach { face ->
                        val vertexData = face.split("/")
                        indices.add(vertexData[0].toInt() - 1)  // OBJ indexing starts from 1
                    }
                }
            }
        }

        return ObjModel(vertices, texCoords, if (normals.isEmpty()) calculateNormals(vertices, indices) else normals, indices)
    }

    fun calculateFaceNormal(v0: List<Float>, v1: List<Float>, v2: List<Float>): List<Float> {
        // Edge vectors
        val edge1 = listOf(v1[0] - v0[0], v1[1] - v0[1], v1[2] - v0[2])
        val edge2 = listOf(v2[0] - v0[0], v2[1] - v0[1], v2[2] - v0[2])

        // Cross product of edge1 and edge2
        val normal = listOf(
            edge1[1] * edge2[2] - edge1[2] * edge2[1],
            edge1[2] * edge2[0] - edge1[0] * edge2[2],
            edge1[0] * edge2[1] - edge1[1] * edge2[0]
        )

        // Normalize the normal vector
        val length = Math.sqrt((normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]).toDouble()).toFloat()
        return listOf(normal[0] / length, normal[1] / length, normal[2] / length)
    }

    fun calculateNormals(vertices: MutableList<Float>, indices: List<Int>): MutableList<Float> {
        val normals = MutableList(vertices.size) { 0.0f } // Initialize normals to 0

        val numFaces = indices.size / 3 // Number of faces

        for (i in 0 until numFaces) {
            // Get the indices of the vertices forming this face
            val i0 = indices[i * 3]
            val i1 = indices[i * 3 + 1]
            val i2 = indices[i * 3 + 2]

            // Get the vertices for the face
            val v0 = vertices.subList(i0 * 3, i0 * 3 + 3)
            val v1 = vertices.subList(i1 * 3, i1 * 3 + 3)
            val v2 = vertices.subList(i2 * 3, i2 * 3 + 3)

            // Calculate the face normal
            val normal = calculateFaceNormal(v0, v1, v2)

            // Accumulate the normal for each vertex in this face
            for (j in 0..2) {
                normals[i0 * 3 + j] += normal[j]
                normals[i1 * 3 + j] += normal[j]
                normals[i2 * 3 + j] += normal[j]
            }
        }

        // Normalize the normals for each vertex
        for (i in 0 until vertices.size / 3) {
            val nx = normals[i * 3]
            val ny = normals[i * 3 + 1]
            val nz = normals[i * 3 + 2]
            val length = Math.sqrt((nx * nx + ny * ny + nz * nz).toDouble()).toFloat()

            normals[i * 3] = nx / length
            normals[i * 3 + 1] = ny / length
            normals[i * 3 + 2] = nz / length
        }

        return normals
    }
}