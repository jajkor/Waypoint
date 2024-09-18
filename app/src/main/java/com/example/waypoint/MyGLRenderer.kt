package com.example.waypoint

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

// Controls what gets drawn on the GLSurfaceView it is associated with
class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private var objModel: ObjModel? = null
    private lateinit var shaderProgram: ShaderProgram

    private var cameraX: Float = 0.0f
    private var cameraY: Float = 0.0f
    private var cameraZ: Float = -5.0f
    private var rotationX: Float = 0.0f
    private var rotationY: Float = 0.0f
    private var scaleFactor: Float = 1.0f

    private lateinit var viewMatrix: FloatArray
    private lateinit var projectionMatrix: FloatArray
    private lateinit var modelMatrix: FloatArray

    init {
        viewMatrix = FloatArray(16)
        projectionMatrix = FloatArray(16)
        modelMatrix = FloatArray(16)
    }

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

        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        Matrix.setLookAtM(viewMatrix, 0, cameraX, cameraY, cameraZ, 0f, 0f, 0f, 0f, 1f, 0f)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, rotationX, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f)
        Matrix.scaleM(modelMatrix, 0, scaleFactor, scaleFactor, scaleFactor)

        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0)

        val uMVPMatrixLocation = GLES30.glGetUniformLocation(shaderProgram.getProgram(), "u_MVPMatrix")
        GLES30.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0)

        objModel?.render(shaderProgram, mvpMatrix)
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f,7f)
    }

    fun onScroll(distanceX: Float, distanceY: Float) {
        rotationX -= distanceY / 10.0f
        rotationY -= distanceX / 10.0f
    }

    fun onScale(scaleFactor: Float) {
        this.scaleFactor *= scaleFactor
    }
}


