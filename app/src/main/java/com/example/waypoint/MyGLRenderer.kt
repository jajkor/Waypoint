package com.example.waypoint

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

// Controls what gets drawn on the GLSurfaceView it is associated with
class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private var objModel: ObjModel? = null

    private lateinit var shaderProgram: ShaderProgram

    // Called once to set up the view's OpenGL ES environment
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f) // Set the background frame color
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        shaderProgram = ShaderProgram(context)
        objModel = ModelLoader(context).loadModel("bunny.obj")
    }

    // Called for each redraw of the view
    override fun onDrawFrame(unused: GL10?) {
        GLES30.glUseProgram(shaderProgram.getProgram()) // Add program to OpenGL ES environment

        GLES30.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        objModel?.render()
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }
}


