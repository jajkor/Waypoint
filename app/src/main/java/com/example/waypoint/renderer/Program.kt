package com.example.waypoint.renderer

import android.opengl.GLES30.GL_FRAGMENT_SHADER
import android.opengl.GLES30.GL_VERTEX_SHADER
import android.opengl.GLES30.glAttachShader
import android.opengl.GLES30.glCompileShader
import android.opengl.GLES30.glCreateProgram
import android.opengl.GLES30.glCreateShader
import android.opengl.GLES30.glDeleteShader
import android.opengl.GLES30.glGetUniformLocation
import android.opengl.GLES30.glLinkProgram
import android.opengl.GLES30.glShaderSource
import android.opengl.GLES30.glUniform1f
import android.opengl.GLES30.glUniform3f
import android.opengl.GLES30.glUniformMatrix4fv
import android.opengl.GLES30.glUseProgram
import com.example.waypoint.Vector3

class Program(
    vertexShaderCode: String?,
    fragmentShaderCode: String?
) {
    private var id: Int

    init {
        val shaders = mutableListOf<Int>()
        shaders.add(loadShader(GL_VERTEX_SHADER, vertexShaderCode))
        shaders.add(loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode))

        // create empty OpenGL ES Program
        id =
            glCreateProgram().also {
                for (i in shaders.indices) {
                    glAttachShader(it, shaders[i])
                }

                glLinkProgram(it) // creates OpenGL ES program executables

                for (i in shaders.indices) {
                    glDeleteShader(shaders[i])
                }
            }
    }

    private fun loadShader(
        type: Int,
        shaderCode: String?
    ): Int {
        // create a vertex shader type (GL_VERTEX_SHADER) or a fragment shader type (GL_FRAGMENT_SHADER)
        return glCreateShader(type).also { shader ->
            // add the source code to the shader and compile it
            glShaderSource(shader, shaderCode)
            glCompileShader(shader)
        }
    }

    fun setFloat(
        uniformName: String,
        f: Float
    ) = glUniform1f(glGetUniformLocation(id, uniformName), f)

    fun setVector3(
        uniformName: String,
        f3: Vector3
    ) = glUniform3f(glGetUniformLocation(id, uniformName), f3.x, f3.y, f3.z)

    fun setMat4(
        uniformName: String,
        m4: FloatArray,
        transpose: Boolean = false
    ) = glUniformMatrix4fv(glGetUniformLocation(id, uniformName), 1, transpose, m4, 0)

    fun use() = glUseProgram(id)
}
