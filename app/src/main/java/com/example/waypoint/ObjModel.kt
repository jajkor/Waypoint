package com.example.waypoint

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

class ObjModel(private val vertices: List<Float>, private val texCoords: List<Float>, private val normals: List<Float>, private val indices: List<Int>) {
    private val vertexBuffer: IntArray = IntArray(1)
    private val normalBuffer: IntArray = IntArray(1)
    private val indexBuffer: IntArray = IntArray(1)

    init {
        // Create and bind buffers for vertex data and indices
        GLES30.glGenBuffers(1, vertexBuffer, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBuffer[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.size * 4, vertices.toFloatBuffer(), GLES30.GL_STATIC_DRAW)

        GLES30.glGenBuffers(1, normalBuffer, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, normalBuffer[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, normals.size * 4, normals.toFloatBuffer(), GLES30.GL_STATIC_DRAW)

        GLES30.glGenBuffers(1, indexBuffer, 0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0])
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indices.size * 4, indices.toIntBuffer(), GLES30.GL_STATIC_DRAW)
    }

    fun render(shaderProgram: ShaderProgram, mvpMatrix: FloatArray) {
        // Enable attributes and bind buffers
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBuffer[0])
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glEnableVertexAttribArray(1)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, normalBuffer[0])
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0])

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.size, GLES30.GL_UNSIGNED_INT, 0)
        GLES30.glDisableVertexAttribArray(0)
    }
}

// Extension function to convert lists to buffers
private fun List<Float>.toFloatBuffer(): FloatBuffer {
    val buffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
    buffer.put(this.toFloatArray()).position(0)
    return buffer
}

private fun List<Int>.toIntBuffer(): IntBuffer {
    val buffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asIntBuffer()
    buffer.put(this.toIntArray()).position(0)
    return buffer
}