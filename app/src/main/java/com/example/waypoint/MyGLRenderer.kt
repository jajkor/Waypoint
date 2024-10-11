package com.example.waypoint

import android.content.Context
import android.opengl.GLES30.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL10.GL_MULTISAMPLE

class MyGLRenderer(private val context: Context, private val camera: Camera3D) : GLSurfaceView.Renderer {
    private var objModel: ObjModel? = null
    private var lightModel: ObjModel? = null
    private var gridModel: ObjModel? = null

    private lateinit var gridProgram: Program
    private lateinit var shaderProgram: Program
    private lateinit var outlineProgram: Program
    private lateinit var lightShaderProgram: Program

    private var startTime = System.nanoTime()

    private var viewMatrix: FloatArray
    private var projectionMatrix: FloatArray
    private var modelMatrix: FloatArray

    init {
        viewMatrix = FloatArray(16)
        projectionMatrix = FloatArray(16)
        modelMatrix = FloatArray(16)
    }

    // Called once to set up the view's OpenGL ES environment
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f) // Set the background frame color

        glEnable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        glDepthFunc(GL_LESS)
        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_NOTEQUAL, 1, 0xFF);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE) // Control result of passing/failing a test.

        gridProgram = Program(
            context.resources.readRawTextFile(R.raw.grid_vert),
            context.resources.readRawTextFile(R.raw.grid_frag)
        )
        gridModel = ModelLoader(context).loadModel("grid/quad.obj")

        shaderProgram = Program(
            context.resources.readRawTextFile(R.raw.campus_vert),
            context.resources.readRawTextFile(R.raw.campus_frag)
        )
        outlineProgram = Program(
            context.resources.readRawTextFile(R.raw.outline_vert),
            context.resources.readRawTextFile(R.raw.outline_frag)
        )
        objModel = ModelLoader(context).loadModel("campus/campus_walls.obj")

        lightShaderProgram = Program(
            context.resources.readRawTextFile(R.raw.light_vert),
            context.resources.readRawTextFile(R.raw.light_frag)
        )
        lightModel = ModelLoader(context).loadModel("light/light_cube.obj")
    }

    // Called for each redraw of the view
    override fun onDrawFrame(unused: GL10?) {
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_STENCIL_TEST)
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)

        //glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        glClearColor(112/255f, 128/255f, 144/255f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)

        viewMatrix = camera.getViewMatrix()

        val elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000.0f
        val lightPosition = Vector3(
            Math.sin(elapsedTime.toDouble()).toFloat() * 20f,
            20.0f,
            Math.cos(elapsedTime.toDouble()).toFloat() * 20f
        )

        //camera.setPivot(Vector3(lightPosition.x, 0.0f, lightPosition.z))

        glStencilMask(0x00)
        gridProgram.use()
        Matrix.setIdentityM(modelMatrix, 0)
        gridProgram.setMat4("u_Model", modelMatrix)
        gridProgram.setMat4("u_View", viewMatrix)
        gridProgram.setMat4("u_Projection", projectionMatrix)
        gridModel?.render()

        glStencilMask(0xFF)
        lightShaderProgram.use()
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, 0.2f, 0.2f, 0.2f)
        Matrix.translateM(modelMatrix, 0, lightPosition.x, lightPosition.y, lightPosition.z)
        lightShaderProgram.setMat4("u_Model", modelMatrix)
        lightShaderProgram.setMat4("u_View", viewMatrix)
        lightShaderProgram.setMat4("u_Projection", projectionMatrix)
        lightModel?.render()

        // 1st. render pass: Render the object and update the stencil buffer
        glStencilFunc(GL_ALWAYS, 1, 0xFF)  // Always pass the stencil test
        glStencilMask(0xFF)  // Enable writing to stencil buffer
        shaderProgram.use()
        Matrix.setIdentityM(modelMatrix, 0)
        //Matrix.translateM(modelMatrix, 0, 0.0f, 1.25f, 0.0f)
        shaderProgram.setMat4("u_Model", modelMatrix)
        shaderProgram.setMat4("u_View", viewMatrix)
        shaderProgram.setMat4("u_Projection", projectionMatrix)
        shaderProgram.setFloat3("lightColor", Vector3(1.0f, 1.0f, 1.0f))
        shaderProgram.setFloat3("surfaceColor", Vector3(211/255f, 211/255f, 211/255f))
        shaderProgram.setFloat("diffuseWarm", 0.3f)
        shaderProgram.setFloat("diffuseCool", 0.3f)
        shaderProgram.setFloat3("warmColor", Vector3(255/255f, 204/255f, 153/255f))
        shaderProgram.setFloat3("coolColor", Vector3(0.0f, 0.0f, 0.6f))
        shaderProgram.setFloat3("lightPos", lightPosition)
        shaderProgram.setFloat3("viewPos", camera.position)
        objModel?.render()

        // Second Pass: Render outline, only where stencil is not equal to 1 (outside the cube)
        glStencilFunc(GL_NOTEQUAL, 1, 0xFF)  // Only pass where stencil value is not 1
        glStencilMask(0x00)  // Disable writing to the stencil buffer
        glDisable(GL_DEPTH_TEST)  // Disable depth testing for the outline
        outlineProgram.use()
        Matrix.setIdentityM(modelMatrix, 0)
        //Matrix.translateM(modelMatrix, 0, 0.0f, 1.25f, 0.0f)
        outlineProgram.setMat4("u_Model", modelMatrix)
        outlineProgram.setMat4("u_View", viewMatrix)
        outlineProgram.setMat4("u_Projection", projectionMatrix)
        outlineProgram.setFloat("u_Outline", 0.05f)
        objModel?.render()

        // Reset stencil and depth test states
        glStencilMask(0xFF)  // Re-enable stencil writing
        glStencilFunc(GL_ALWAYS, 1, 0xFF)  // Reset stencil function
        glEnable(GL_DEPTH_TEST)  // Re-enable depth test
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3.0f,100f)
    }
}