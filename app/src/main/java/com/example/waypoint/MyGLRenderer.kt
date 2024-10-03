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

    private lateinit var shaderProgram: ShaderProgram
    private lateinit var outlineProgram: ShaderProgram
    private lateinit var lightShaderProgram: ShaderProgram

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

        shaderProgram = ShaderProgram(
            context.resources.openRawResource(R.raw.vertex_shader).readBytes().toString(Charset.defaultCharset()),
            context.resources.openRawResource(R.raw.fragment_shader).readBytes().toString(Charset.defaultCharset())
        )
        outlineProgram = ShaderProgram(
            context.resources.openRawResource(R.raw.vertex_outline_shader).readBytes().toString(Charset.defaultCharset()),
            context.resources.openRawResource(R.raw.fragment_outline_shader).readBytes().toString(Charset.defaultCharset())
        )
        objModel = ModelLoader(context).loadModel("bunny.obj")

        lightShaderProgram = ShaderProgram(
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

        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        //GLES30.glClearColor(0.00392156862745098f, 0.13725490196078433f, 0.25098039215686274f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_STENCIL_BUFFER_BIT)

        Matrix.setLookAtM(viewMatrix, 0, cameraX, cameraY, cameraZ, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, rotationX, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f)
        Matrix.scaleM(modelMatrix, 0, scaleFactor, scaleFactor, scaleFactor)

        val elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000.0f
        val lightX = Math.sin(elapsedTime.toDouble()).toFloat() * 20f // Move along the x-axis
        val lightY = 20.0f
        val lightZ = Math.cos(elapsedTime.toDouble()).toFloat() * 20f// Move along the z-axis

        GLES30.glStencilMask(0xFF)
        GLES30.glUseProgram(lightShaderProgram.getProgram())
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, 0.2f, 0.2f, 0.2f)
        Matrix.scaleM(modelMatrix, 0, scaleFactor, scaleFactor, scaleFactor)
        Matrix.rotateM(modelMatrix, 0, rotationX, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f)
        Matrix.translateM(modelMatrix, 0, lightX, lightY, lightZ)
        val uLightModelMatrixLocation = GLES30.glGetUniformLocation(lightShaderProgram.getProgram(), "u_Model")
        GLES30.glUniformMatrix4fv(uLightModelMatrixLocation, 1, false, modelMatrix, 0)
        val uLightViewMatrixLocation = GLES30.glGetUniformLocation(lightShaderProgram.getProgram(), "u_View")
        GLES30.glUniformMatrix4fv(uLightViewMatrixLocation, 1, false, viewMatrix, 0)
        val uLightProjectionMatrixLocation = GLES30.glGetUniformLocation(lightShaderProgram.getProgram(), "u_Projection")
        GLES30.glUniformMatrix4fv(uLightProjectionMatrixLocation, 1, false, projectionMatrix, 0)
        lightModel?.render(lightShaderProgram, modelMatrix)

        // 1st. render pass: Render the object and update the stencil buffer
        GLES30.glStencilFunc(GLES30.GL_ALWAYS, 1, 0xFF)  // Always pass the stencil test
        GLES30.glStencilMask(0xFF)  // Enable writing to stencil buffer
        GLES30.glUseProgram(shaderProgram.getProgram()) // Add program to OpenGL ES environment
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, scaleFactor, scaleFactor, scaleFactor)
        Matrix.rotateM(modelMatrix, 0, rotationX, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f)
        val uModelMatrixLocation = GLES30.glGetUniformLocation(shaderProgram.getProgram(), "u_Model")
        GLES30.glUniformMatrix4fv(uModelMatrixLocation, 1, false, modelMatrix, 0)
        val uViewMatrixLocation = GLES30.glGetUniformLocation(shaderProgram.getProgram(), "u_View")
        GLES30.glUniformMatrix4fv(uViewMatrixLocation, 1, false, viewMatrix, 0)
        val uProjectionMatrixLocation = GLES30.glGetUniformLocation(shaderProgram.getProgram(), "u_Projection")
        GLES30.glUniformMatrix4fv(uProjectionMatrixLocation, 1, false, projectionMatrix, 0)
        val lightColorLocation = GLES30.glGetUniformLocation(shaderProgram.getProgram(), "lightColor")
        GLES30.glUniform3f(lightColorLocation, 1.0f, 1.0f, 1.0f)
        val surfaceColorLocation = GLES30.glGetUniformLocation(shaderProgram.getProgram(), "surfaceColor")
        GLES30.glUniform3f(surfaceColorLocation, 0.75f, 0.75f, 0.75f)
        val diffuseWarmLocation = GLES30.glGetUniformLocation(shaderProgram.getProgram(), "diffuseWarm")
        GLES30.glUniform1f(diffuseWarmLocation, 0.45f)
        val diffuseCoolLocation = GLES30.glGetUniformLocation(shaderProgram.getProgram(), "diffuseCool")
        GLES30.glUniform1f(diffuseCoolLocation, 0.45f)
        val warmColorLocation = GLES30.glGetUniformLocation(shaderProgram.getProgram(), "warmColor")
        GLES30.glUniform3f(warmColorLocation, 0.6f, 0.6f, 0.0f)
        val coolColorLocation = GLES30.glGetUniformLocation(shaderProgram.getProgram(), "coolColor")
        GLES30.glUniform3f(coolColorLocation, 0.0f, 0.0f, 0.6f)
        val lightPositionLocation = GLES30.glGetUniformLocation(shaderProgram.getProgram(), "lightPos")
        GLES30.glUniform3f(lightPositionLocation, lightX, lightY, lightZ)
        val viewPositionLocation = GLES30.glGetUniformLocation(shaderProgram.getProgram(), "viewPos")
        val cameraPosition = getCameraPositionInWorldSpace(viewMatrix)
        GLES30.glUniform3f(viewPositionLocation, cameraPosition[0], cameraPosition[1], cameraPosition[2])
        objModel?.render(shaderProgram, modelMatrix)

        // Second Pass: Render outline, only where stencil is not equal to 1 (outside the cube)
        GLES30.glStencilFunc(GLES30.GL_NOTEQUAL, 1, 0xFF)  // Only pass where stencil value is not 1
        GLES30.glStencilMask(0x00)  // Disable writing to the stencil buffer
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)  // Disable depth testing for the outline

        GLES30.glUseProgram(outlineProgram.getProgram())
        Matrix.setIdentityM(modelMatrix, 0)
        //Matrix.scaleM(modelMatrix, 0, 1.05f, 1.05f, 1.05f)
        Matrix.scaleM(modelMatrix, 0, scaleFactor, scaleFactor, scaleFactor)
        Matrix.rotateM(modelMatrix, 0, rotationX, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f)
        val ouModelMatrixLocation = GLES30.glGetUniformLocation(outlineProgram.getProgram(), "u_Model")
        GLES30.glUniformMatrix4fv(ouModelMatrixLocation, 1, false, modelMatrix, 0)
        val ouViewMatrixLocation = GLES30.glGetUniformLocation(outlineProgram.getProgram(), "u_View")
        GLES30.glUniformMatrix4fv(ouViewMatrixLocation, 1, false, viewMatrix, 0)
        val ouProjectionMatrixLocation = GLES30.glGetUniformLocation(outlineProgram.getProgram(), "u_Projection")
        GLES30.glUniformMatrix4fv(ouProjectionMatrixLocation, 1, false, projectionMatrix, 0)
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


