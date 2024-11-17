package com.example.waypoint.renderer.model

import android.opengl.GLES30.GL_ARRAY_BUFFER
import android.opengl.GLES30.GL_ELEMENT_ARRAY_BUFFER
import android.opengl.GLES30.GL_FLOAT
import android.opengl.GLES30.GL_LINES
import android.opengl.GLES30.GL_STATIC_DRAW
import android.opengl.GLES30.GL_TRIANGLES
import android.opengl.GLES30.GL_UNSIGNED_INT
import android.opengl.GLES30.glBindBuffer
import android.opengl.GLES30.glBufferData
import android.opengl.GLES30.glDisableVertexAttribArray
import android.opengl.GLES30.glDrawArrays
import android.opengl.GLES30.glDrawElements
import android.opengl.GLES30.glEnableVertexAttribArray
import android.opengl.GLES30.glGenBuffers
import android.opengl.GLES30.glVertexAttribPointer
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Model(
    private val vertices: FloatBuffer,
    private val normals: FloatBuffer,
    private val texCoords: FloatBuffer,
    private val indices: IntBuffer,
    private val material: Material,
) {
    private val vertexBuffer: IntArray = IntArray(1)
    private val normalBuffer: IntArray = IntArray(1)
    private val texCoordBuffer: IntArray = IntArray(1)
    private val indexBuffer: IntArray = IntArray(1)

    private val coordsPerVertex: Int = 3

    fun getMaterial(): Material = material

    init {
        // Create and bind buffers for vertex data and indices
        glGenBuffers(1, vertexBuffer, 0)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0])
        glBufferData(GL_ARRAY_BUFFER, vertices.capacity() * Float.SIZE_BYTES, vertices, GL_STATIC_DRAW)

        glGenBuffers(1, normalBuffer, 0)
        glBindBuffer(GL_ARRAY_BUFFER, normalBuffer[0])
        glBufferData(GL_ARRAY_BUFFER, normals.capacity() * Float.SIZE_BYTES, normals, GL_STATIC_DRAW)

        glGenBuffers(1, texCoordBuffer, 0)
        glBindBuffer(GL_ARRAY_BUFFER, texCoordBuffer[0])
        glBufferData(GL_ARRAY_BUFFER, texCoords.capacity() * Float.SIZE_BYTES, texCoords, GL_STATIC_DRAW)

        glGenBuffers(1, indexBuffer, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.capacity() * Int.SIZE_BYTES, indices, GL_STATIC_DRAW)
    }

    fun draw(primitiveType: Int) {
        // Enable attributes and bind buffers
        glEnableVertexAttribArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0])
        glVertexAttribPointer(0, coordsPerVertex, GL_FLOAT, false, 0, 0)

        glEnableVertexAttribArray(1)
        glBindBuffer(GL_ARRAY_BUFFER, normalBuffer[0])
        glVertexAttribPointer(1, coordsPerVertex, GL_FLOAT, false, 0, 0)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0])

        if (primitiveType == GL_TRIANGLES) {
            glDrawElements(GL_TRIANGLES, indices.capacity(), GL_UNSIGNED_INT, 0)
        } else {
            glDrawArrays(GL_LINES, 0, vertices.capacity())
        }

        glDisableVertexAttribArray(0)
    }
}
