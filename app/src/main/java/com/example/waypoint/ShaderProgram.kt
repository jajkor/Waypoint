package com.example.waypoint

import android.content.Context
import android.opengl.GLES20
import java.nio.charset.Charset

class ShaderProgram(vertexShaderCode: String, fragmentShaderCode: String) {
/*
    private val vertexShaderCode: String =
        context.resources.openRawResource(R.raw.vertex_shader).readBytes().toString(Charset.defaultCharset())
    private val fragmentShaderCode: String =
        context.resources.openRawResource(R.raw.fragment_shader).readBytes().toString(Charset.defaultCharset())
*/

    private var mProgram: Int

    init {
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)// add the vertex shader to program
            GLES20.glAttachShader(it, fragmentShader)// add the fragment shader to program

            GLES20.glLinkProgram(it) // creates OpenGL ES program executables

            GLES20.glDeleteShader(vertexShader)
            GLES20.glDeleteShader(fragmentShader)
        }
    }

    fun getProgram(): Int {
        return mProgram
    }

    fun loadShader(type: Int, shaderCode: String): Int {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER) or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        return GLES20.glCreateShader(type).also { shader ->
            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}