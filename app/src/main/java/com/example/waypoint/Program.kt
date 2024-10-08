package com.example.waypoint

import android.opengl.GLES30.*

class Program(vertexShaderCode: String, fragmentShaderCode: String) {

    private var id: Int

    init {
        val vertexShader: Int = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        id = glCreateProgram().also {
            glAttachShader(it, vertexShader)// add the vertex shader to program
            glAttachShader(it, fragmentShader)// add the fragment shader to program

            glLinkProgram(it) // creates OpenGL ES program executables

            glDeleteShader(vertexShader)
            glDeleteShader(fragmentShader)
        }
    }

    fun loadShader(type: Int, shaderCode: String): Int {
        // create a vertex shader type (GL_VERTEX_SHADER) or a fragment shader type (GL_FRAGMENT_SHADER)
        return glCreateShader(type).also { shader ->
            // add the source code to the shader and compile it
            glShaderSource(shader, shaderCode)
            glCompileShader(shader)
        }
    }

    fun setFloat(uniformName: String, f: Float) = glUniform1f(glGetUniformLocation(id, uniformName), f)

    fun setFloat2(uniformName: String, f2: Vector2) =
        glUniform2f(glGetUniformLocation(id, uniformName), f2.x, f2.y)

    fun setFloat3(uniformName: String, f3: Vector3) =
        glUniform3f(glGetUniformLocation(id, uniformName), f3.x, f3.y, f3.z)

    fun setInt(location: Int, i: Int) = glUniform1i(location, i)

    fun setInt(uniformName: String, i: Int) = glUniform1i(glGetUniformLocation(id, uniformName), i)

    fun setMat4(uniformName: String, m4: FloatArray, transpose: Boolean = false) =
        glUniformMatrix4fv(glGetUniformLocation(id, uniformName), 1, transpose, m4, 0)

    fun use() = glUseProgram(id)
}