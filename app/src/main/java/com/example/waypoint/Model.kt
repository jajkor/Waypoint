package com.example.waypoint

import android.opengl.GLES32.GL_ARRAY_BUFFER
import android.opengl.GLES32.GL_ELEMENT_ARRAY_BUFFER
import android.opengl.GLES32.GL_FLOAT
import android.opengl.GLES32.GL_LINES
import android.opengl.GLES32.GL_STATIC_DRAW
import android.opengl.GLES32.GL_TRIANGLES
import android.opengl.GLES32.GL_UNSIGNED_INT
import android.opengl.GLES32.glBindBuffer
import android.opengl.GLES32.glBufferData
import android.opengl.GLES32.glDisableVertexAttribArray
import android.opengl.GLES32.glDrawArrays
import android.opengl.GLES32.glDrawElements
import android.opengl.GLES32.glDrawElementsInstanced
import android.opengl.GLES32.glEnableVertexAttribArray
import android.opengl.GLES32.glGenBuffers
import android.opengl.GLES32.glVertexAttribPointer

class Model(
    private val vertices: List<Float>,
    private val normals: List<Float>,
    private val texCoords: List<Float>,
    private val indices: List<Int>,
) {
    private val vertexBuffer: IntArray = IntArray(1)
    private val normalBuffer: IntArray = IntArray(1)
    private val texCoordBuffer: IntArray = IntArray(1)
    private val indexBuffer: IntArray = IntArray(1)

    private val coordsPerVertex: Int = 3

    init {
        // Create and bind buffers for vertex data and indices
        glGenBuffers(1, vertexBuffer, 0)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0])
        glBufferData(GL_ARRAY_BUFFER, vertices.size * Float.SIZE_BYTES, vertices.toFloatBuffer(), GL_STATIC_DRAW)

        glGenBuffers(1, normalBuffer, 0)
        glBindBuffer(GL_ARRAY_BUFFER, normalBuffer[0])
        glBufferData(GL_ARRAY_BUFFER, normals.size * Float.SIZE_BYTES, normals.toFloatBuffer(), GL_STATIC_DRAW)

        glGenBuffers(1, texCoordBuffer, 0)
        glBindBuffer(GL_ARRAY_BUFFER, texCoordBuffer[0])
        glBufferData(GL_ARRAY_BUFFER, texCoords.size * Float.SIZE_BYTES, texCoords.toFloatBuffer(), GL_STATIC_DRAW)

        glGenBuffers(1, indexBuffer, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.size * Int.SIZE_BYTES, indices.toIntBuffer(), GL_STATIC_DRAW)
    }

    fun draw() {
        // Enable attributes and bind buffers
        glEnableVertexAttribArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0])
        glVertexAttribPointer(0, coordsPerVertex, GL_FLOAT, false, 0, 0)

        glEnableVertexAttribArray(1)
        glBindBuffer(GL_ARRAY_BUFFER, normalBuffer[0])
        glVertexAttribPointer(1, coordsPerVertex, GL_FLOAT, false, 0, 0)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0])

        glDrawElements(GL_TRIANGLES, indices.size, GL_UNSIGNED_INT, 0)
        glDisableVertexAttribArray(0)
    }

    fun drawPoints() {
        // Enable attributes and bind buffers
        glEnableVertexAttribArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0])
        glVertexAttribPointer(0, coordsPerVertex, GL_FLOAT, false, 0, 0)

        glDrawArrays(GL_LINES, 0, vertices.size)
        glDisableVertexAttribArray(0)
    }

    fun drawInstanced(n: Int) {
        // Enable attributes and bind buffers
        glEnableVertexAttribArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0])
        glVertexAttribPointer(0, coordsPerVertex, GL_FLOAT, false, 0, 0)

        glEnableVertexAttribArray(1)
        glBindBuffer(GL_ARRAY_BUFFER, normalBuffer[0])
        glVertexAttribPointer(1, coordsPerVertex, GL_FLOAT, false, 0, 0)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0])

        glDrawElementsInstanced(GL_TRIANGLES, indices.size, GL_UNSIGNED_INT, 0, n)
        glDisableVertexAttribArray(0)
    }
}
