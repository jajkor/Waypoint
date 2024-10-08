package com.example.waypoint

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.charset.Charset
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

// Controls what gets drawn on the GLSurfaceView it is associated with
class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private var objModel: ObjModel? = null
    private var lightModel: ObjModel? = null

    private lateinit var shaderProgram: Program
    private lateinit var outlineProgram: Program
    private lateinit var lightShaderProgram: Program

    private var cameraX: Float = 0.0f
    private var cameraY: Float = 0.0f
    private var cameraZ: Float = -5.0f

    private var rotationX: Float = 0.0f
    private var rotationY: Float = 0.0f
    private var scaleFactor: Float = 1.0f

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
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f) // Set the background frame color

        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDepthFunc(GLES30.GL_LESS);
        GLES30.glEnable(GLES30.GL_STENCIL_TEST)
        GLES30.glStencilFunc(GLES30.GL_NOTEQUAL, 1, 0xFF);
        GLES30.glStencilOp(GLES30.GL_KEEP, GLES30.GL_KEEP, GLES30.GL_REPLACE) // Control result of passing/failing a test.

/*        shaderProgram = Program.create(
            context.resources.readRawTextFile(R.raw.vertex_shader),
            context.resources.readRawTextFile(R.raw.fragment_shader)
        )
        outlineProgram = Program.create(
            context.resources.readRawTextFile(R.raw.fragment_outline_shader),
            context.resources.readRawTextFile(R.raw.vertex_outline_shader)
        )
        objModel = ModelLoader(context).loadModel("walls_fixed.obj")

        lightShaderProgram = Program.create(
            context.resources.readRawTextFile(R.raw.light_vertex_shader),
            context.resources.readRawTextFile(R.raw.light_fragment_shader)
        )
        lightModel = ModelLoader(context).loadModel("light_cube.obj")*/
        shaderProgram = Program(
            context.resources.openRawResource(R.raw.vertex_shader).readBytes().toString(Charset.defaultCharset()),
            context.resources.openRawResource(R.raw.fragment_shader).readBytes().toString(Charset.defaultCharset())
        )
        outlineProgram = Program(
            context.resources.openRawResource(R.raw.vertex_outline_shader).readBytes().toString(Charset.defaultCharset()),
            context.resources.openRawResource(R.raw.fragment_outline_shader).readBytes().toString(Charset.defaultCharset())
        )
        objModel = ModelLoader(context).loadModel("walls_fixed.obj")

        lightShaderProgram = Program(
            context.resources.openRawResource(R.raw.light_vertex_shader).readBytes().toString(Charset.defaultCharset()),
            context.resources.openRawResource(R.raw.light_fragment_shader).readBytes().toString(Charset.defaultCharset())
        )
        lightModel = ModelLoader(context).loadModel("light_cube.obj")
    }

    // Called for each redraw of the view
    override fun onDrawFrame(unused: GL10?) {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_STENCIL_TEST)
        GLES30.glStencilOp(GLES30.GL_KEEP, GLES30.GL_KEEP, GLES30.GL_REPLACE);

        GLES30.glClearColor(112/255f, 128/255f, 144/255f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_STENCIL_BUFFER_BIT)

        Matrix.setLookAtM(viewMatrix, 0, cameraX, cameraY, cameraZ, 0f, 0f, 0f, 0f, 1f, 0f)

        val elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000.0f
        val lightX = Math.sin(elapsedTime.toDouble()).toFloat() * 20f // Move along the x-axis
        val lightY = 20.0f
        val lightZ = Math.cos(elapsedTime.toDouble()).toFloat() * 20f// Move along the z-axis

        GLES30.glStencilMask(0xFF)
        lightShaderProgram.use()
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, 0.2f, 0.2f, 0.2f)
        Matrix.scaleM(modelMatrix, 0, scaleFactor, scaleFactor, scaleFactor)
        Matrix.rotateM(modelMatrix, 0, rotationX, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f)
        Matrix.translateM(modelMatrix, 0, lightX, lightY, lightZ)
        lightShaderProgram.setMat4("u_Model", modelMatrix)
        lightShaderProgram.setMat4("u_View", viewMatrix)
        lightShaderProgram.setMat4("u_Projection", projectionMatrix)
        lightModel?.render(lightShaderProgram, modelMatrix)

        // 1st. render pass: Render the object and update the stencil buffer
        GLES30.glStencilFunc(GLES30.GL_ALWAYS, 1, 0xFF)  // Always pass the stencil test
        GLES30.glStencilMask(0xFF)  // Enable writing to stencil buffer
        shaderProgram.use()
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, scaleFactor, scaleFactor, scaleFactor)
        Matrix.rotateM(modelMatrix, 0, rotationX, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f)
        shaderProgram.setMat4("u_Model", modelMatrix)
        shaderProgram.setMat4("u_View", viewMatrix)
        shaderProgram.setMat4("u_Projection", projectionMatrix)
        shaderProgram.setFloat3("lightColor", Vector3(1.0f, 1.0f, 1.0f))
        shaderProgram.setFloat3("surfaceColor", Vector3(211/255f, 211/255f, 211/255f))
        shaderProgram.setFloat("diffuseWarm", 0.3f)
        shaderProgram.setFloat("diffuseCool", 0.3f)
        shaderProgram.setFloat3("warmColor", Vector3(255/255f, 204/255f, 153/255f))
        shaderProgram.setFloat3("coolColor", Vector3(0.0f, 0.0f, 0.6f))
        shaderProgram.setFloat3("lightPos", Vector3(lightX, lightY, lightZ))
        val cameraPosition = getCameraPositionInWorldSpace(viewMatrix)
        shaderProgram.setFloat3("viewPos", Vector3(cameraPosition[0], cameraPosition[1], cameraPosition[2]))
        objModel?.render(shaderProgram, modelMatrix)

        // Second Pass: Render outline, only where stencil is not equal to 1 (outside the cube)
        GLES30.glStencilFunc(GLES30.GL_NOTEQUAL, 1, 0xFF)  // Only pass where stencil value is not 1
        GLES30.glStencilMask(0x00)  // Disable writing to the stencil buffer
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)  // Disable depth testing for the outline
        outlineProgram.use()
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, scaleFactor, scaleFactor, scaleFactor)
        Matrix.rotateM(modelMatrix, 0, rotationX, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f)
        outlineProgram.setMat4("u_Model", modelMatrix)
        outlineProgram.setMat4("u_View", viewMatrix)
        outlineProgram.setMat4("u_Projection", projectionMatrix)
        outlineProgram.setFloat("u_Outline", 0.05f)
        objModel?.render(outlineProgram, modelMatrix)

        // Reset stencil and depth test states
        GLES30.glStencilMask(0xFF)  // Re-enable stencil writing
        GLES30.glStencilFunc(GLES30.GL_ALWAYS, 1, 0xFF)  // Reset stencil function
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)  // Re-enable depth test
    }

    fun getCameraPositionInWorldSpace(viewMatrix: FloatArray): FloatArray {
        val inverseViewMatrix = FloatArray(16)

        // Calculate the inverse of the view matrix
        Matrix.invertM(inverseViewMatrix, 0, viewMatrix, 0)

        // Extract the camera position from the inverse view matrix (last column)
        return floatArrayOf(
            inverseViewMatrix[12], // x
            inverseViewMatrix[13], // y
            inverseViewMatrix[14]  // z
        )
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f,10f)
    }

    fun onScroll(distanceX: Float, distanceY: Float) {
        rotationX -= distanceY / 10.0f
        rotationY -= distanceX / 10.0f
    }

    fun onScale(scaleFactor: Float) {
        this.scaleFactor *= scaleFactor
    }
}


