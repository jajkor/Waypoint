package com.example.waypoint

import android.opengl.GLES32.*

class ObjModel(private val vertices: List<Float>, private val normals: List<Float>, private val texCoords: List<Float>, private val indices: List<Int>) {
    private val vertexBuffer: IntArray = IntArray(1)
    private val normalBuffer: IntArray = IntArray(1)
    private val texCoordBuffer: IntArray = IntArray(1)
    private val indexBuffer: IntArray = IntArray(1)

    init {
        // Create and bind buffers for vertex data and indices
        glGenBuffers(1, vertexBuffer, 0)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0])
        glBufferData(GL_ARRAY_BUFFER, vertices.size * 4, vertices.toFloatBuffer(), GL_STATIC_DRAW)

        glGenBuffers(1, normalBuffer, 0)
        glBindBuffer(GL_ARRAY_BUFFER, normalBuffer[0])
        glBufferData(GL_ARRAY_BUFFER, normals.size * 4, normals.toFloatBuffer(), GL_STATIC_DRAW)

        glGenBuffers(1, texCoordBuffer, 0)
        glBindBuffer(GL_ARRAY_BUFFER, texCoordBuffer[0])
        glBufferData(GL_ARRAY_BUFFER, texCoords.size * 4, texCoords.toFloatBuffer(), GL_STATIC_DRAW)

        glGenBuffers(1, indexBuffer, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.size * 4, indices.toIntBuffer(), GL_STATIC_DRAW)
    }

    fun draw() {
        // Enable attributes and bind buffers
        glEnableVertexAttribArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0])
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)

        glEnableVertexAttribArray(1)
        glBindBuffer(GL_ARRAY_BUFFER, normalBuffer[0])
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0])

        glDrawElements(GL_TRIANGLES, indices.size, GL_UNSIGNED_INT, 0)
        glDisableVertexAttribArray(0)
    }

    fun drawPoints() {
        // Enable attributes and bind buffers
        glEnableVertexAttribArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0])
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)

        glDrawArrays(GL_LINES, 0, 2)
        glDisableVertexAttribArray(0)
    }

    fun drawInstanced(n: Int) {
        // Enable attributes and bind buffers
        glEnableVertexAttribArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer[0])
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)

        glEnableVertexAttribArray(1)
        glBindBuffer(GL_ARRAY_BUFFER, normalBuffer[0])
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0])

        glDrawElementsInstanced(GL_TRIANGLES,  indices.size, GL_UNSIGNED_INT, 0, n)
        glDisableVertexAttribArray(0)
    }
}