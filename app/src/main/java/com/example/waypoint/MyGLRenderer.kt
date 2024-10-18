package com.example.waypoint

import android.content.Context
import android.opengl.GLES32.GL_ALWAYS
import android.opengl.GLES32.GL_BLEND
import android.opengl.GLES32.GL_COLOR_BUFFER_BIT
import android.opengl.GLES32.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES32.GL_DEPTH_TEST
import android.opengl.GLES32.GL_KEEP
import android.opengl.GLES32.GL_LESS
import android.opengl.GLES32.GL_MAJOR_VERSION
import android.opengl.GLES32.GL_MINOR_VERSION
import android.opengl.GLES32.GL_NOTEQUAL
import android.opengl.GLES32.GL_ONE_MINUS_SRC_ALPHA
import android.opengl.GLES32.GL_REPLACE
import android.opengl.GLES32.GL_SRC_ALPHA
import android.opengl.GLES32.GL_STENCIL_BUFFER_BIT
import android.opengl.GLES32.GL_STENCIL_TEST
import android.opengl.GLES32.glBlendFunc
import android.opengl.GLES32.glClear
import android.opengl.GLES32.glClearColor
import android.opengl.GLES32.glDepthFunc
import android.opengl.GLES32.glDisable
import android.opengl.GLES32.glEnable
import android.opengl.GLES32.glGetIntegerv
import android.opengl.GLES32.glStencilFunc
import android.opengl.GLES32.glStencilMask
import android.opengl.GLES32.glStencilOp
import android.opengl.GLES32.glViewport
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(
    private val context: Context,
    private val camera: Camera3D,
) : GLSurfaceView.Renderer {
    private var campusModel: Model? = null
    private var pathModel: Model? = null
    private var cubeModel: Model? = null
    private var gridQuad: Model? = null

    private lateinit var gridShader: Program
    private lateinit var campusShader: Program
    private lateinit var outlineShader: Program
    private lateinit var pathShader: Program
    private lateinit var lightShader: Program
    private lateinit var displayNormalsShader: Program

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
    override fun onSurfaceCreated(
        unused: GL10,
        config: EGLConfig,
    ) {
        val glVersion = IntArray(2)
        glGetIntegerv(GL_MAJOR_VERSION, glVersion, 0)
        glGetIntegerv(GL_MINOR_VERSION, glVersion, 1)

        if (glVersion[0] >= 3 && glVersion[1] >= 2) {
            // Set up for OpenGL ES 3.2 specific features (like geometry shaders)
            Log.i("OpenGL", "OpenGL ES 3.2 supported")
        } else {
            throw RuntimeException("OpenGL ES 3.2 is not supported on this device")
        }

        glEnable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        glDepthFunc(GL_LESS)
        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_NOTEQUAL, 1, 0xFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE) // Control result of passing/failing a test.

        gridShader =
            Program(
                context.resources.readRawTextFile(R.raw.grid_vert),
                context.resources.readRawTextFile(R.raw.grid_frag),
            )
        gridQuad = ModelLoader(context).loadModel("grid/quad.obj")

        campusShader =
            Program(
                context.resources.readRawTextFile(R.raw.campus_vert),
                context.resources.readRawTextFile(R.raw.campus_frag),
            )
        outlineShader =
            Program(
                context.resources.readRawTextFile(R.raw.outline_vert),
                context.resources.readRawTextFile(R.raw.outline_frag),
            )
        campusModel = ModelLoader(context).loadModel("campus/1stfloor.obj")
        // campusModel = ModelLoader(context).loadModel("examples/teapot.obj")

        pathShader =
            Program(
                context.resources.readRawTextFile(R.raw.path_vert),
                context.resources.readRawTextFile(R.raw.path_frag),
                context.resources.readRawTextFile(R.raw.path_geom),
            )
        pathModel = ModelLoader(context).loadModel("path.obj")

        lightShader =
            Program(
                context.resources.readRawTextFile(R.raw.light_vert),
                context.resources.readRawTextFile(R.raw.light_frag),
            )
        cubeModel = ModelLoader(context).loadModel("cube/cube.obj")

        displayNormalsShader =
            Program(
                context.resources.readRawTextFile(R.raw.display_normals_vert),
                context.resources.readRawTextFile(R.raw.display_normals_frag),
                context.resources.readRawTextFile(R.raw.display_normals_geom),
            )
    }

    // Called for each redraw of the view
    override fun onDrawFrame(unused: GL10?) {
        glClearColor(0.9490196078431372f, 0.9490196078431372f, 0.9490196078431372f, 1.0f) // Set the background frame colors
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)

        viewMatrix = camera.getViewMatrix()

        val elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000.0f
        val lightPosition =
            Vector3(
                Math.sin(elapsedTime.toDouble()).toFloat() * 20f,
                30.0f,
                Math.cos(elapsedTime.toDouble()).toFloat() * 20f,
            )

        glStencilMask(0x00)
        pathShader.use()
        Matrix.setIdentityM(modelMatrix, 0)
        pathShader.setVector3("u_UserPos", Vector3(0.000f, 0.000f, 0.000f))
        pathShader.setVector3("u_NodePos", Vector3(10.000f, 0.000f, 0.000f))
        pathShader.setMat4("u_View", viewMatrix)
        pathShader.setMat4("u_Projection", projectionMatrix)
        pathModel?.drawPoints()

        glStencilMask(0x00)
        gridShader.use()
        Matrix.setIdentityM(modelMatrix, 0)
        gridShader.setMat4("u_Model", modelMatrix)
        gridShader.setMat4("u_View", viewMatrix)
        gridShader.setMat4("u_Projection", projectionMatrix)
        gridQuad?.draw()

        glStencilMask(0xFF)
        lightShader.use()
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, 0.2f, 0.2f, 0.2f)
        Matrix.translateM(modelMatrix, 0, lightPosition.x, lightPosition.y, lightPosition.z)
        lightShader.setMat4("u_Model", modelMatrix)
        lightShader.setMat4("u_View", viewMatrix)
        lightShader.setMat4("u_Projection", projectionMatrix)
        cubeModel?.draw()

        // 1st. render pass: Render the object and update the stencil buffer
        glStencilFunc(GL_ALWAYS, 1, 0xFF) // Always pass the stencil test
        glStencilMask(0xFF) // Enable writing to stencil buffer
        campusShader.use()
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.0f, 1.25f, 0.0f)
        campusShader.setMat4("u_Model", modelMatrix)
        campusShader.setMat4("u_View", viewMatrix)
        campusShader.setMat4("u_Projection", projectionMatrix)
        campusShader.setVector3("lightColor", Vector3(1.0f, 1.0f, 1.0f))
        campusShader.setVector3("surfaceColor", Vector3(0.0196078431372549f, 0.7803921568627451f, 0.9490196078431372f))
        campusShader.setFloat("diffuseWarm", 0.3f)
        campusShader.setFloat("diffuseCool", 0.3f)
        campusShader.setVector3("warmColor", Vector3(0.9490196078431372f, 0.47058823529411764f, 0.047058823529411764f))
        campusShader.setVector3("coolColor", Vector3(0.49019607843137253f, 0.3607843137254902f, 0.9490196078431372f))
        campusShader.setVector3("lightPos", lightPosition)
        campusShader.setVector3("viewPos", camera.position)
        campusModel?.draw()

        // Enable to draw normals of a mesh
        displayNormalsShader.use()
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.0f, 1.25f, 0.0f)
        campusShader.setMat4("u_Model", modelMatrix)
        campusShader.setMat4("u_View", viewMatrix)
        campusShader.setMat4("u_Projection", projectionMatrix)
        campusModel?.draw()

        // Second Pass: Render outline, only where stencil is not equal to 1 (outside the cube)
        glStencilFunc(GL_NOTEQUAL, 1, 0xFF) // Only pass where stencil value is not 1
        glStencilMask(0x00) // Disable writing to the stencil buffer
        glDisable(GL_DEPTH_TEST) // Disable depth testing for the outline
        outlineShader.use()
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.0f, 1.25f, 0.0f)
        outlineShader.setMat4("u_Model", modelMatrix)
        outlineShader.setMat4("u_View", viewMatrix)
        outlineShader.setMat4("u_Projection", projectionMatrix)
        outlineShader.setFloat("u_Outline", 0.05f)
        campusModel?.draw()

        // Reset stencil and depth test states
        glStencilMask(0xFF) // Re-enable stencil writing
        glStencilFunc(GL_ALWAYS, 1, 0xFF) // Reset stencil function
        glEnable(GL_DEPTH_TEST) // Re-enable depth test
    }

    override fun onSurfaceChanged(
        unused: GL10?,
        width: Int,
        height: Int,
    ) {
        glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 100f)
    }
}
