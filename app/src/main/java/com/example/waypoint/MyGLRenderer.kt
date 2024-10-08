package com.example.waypoint

import Vector3
import android.content.Context
import android.opengl.GLES30.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.waypoint.model.DefaultProgramLocations
import com.example.waypoint.model.Model
import com.example.waypoint.model.ObjLoader
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

// Controls what gets drawn on the GLSurfaceView it is associated with
class MyGLRenderer(private val context: Context, private val camera: Camera) : GLSurfaceView.Renderer {
    private lateinit var model: Model

    private lateinit var program: Program

    private var viewMatrix: FloatArray
    private var projectionMatrix: FloatArray
    private var modelMatrix: FloatArray

    private var rotationX: Float = 0.0f
    private var rotationY: Float = 0.0f
    private var scaleFactor: Float = 1.0f

    private var width: Int = 0
    private var height: Int = 0
    private val timer = Timer()

    init {
        viewMatrix = FloatArray(16)
        projectionMatrix = FloatArray(16)
        modelMatrix = FloatArray(16)
    }

    // Called once to set up the view's OpenGL ES environment
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f) // Set the background frame color

        glEnable(GL_DEPTH_TEST)

        program = Program.create(context.resources.readRawTextFile(R.raw.vertex_shader), context.resources.readRawTextFile(R.raw.fragment_shader))
        program.use()

        model = ObjLoader.fromAssets(
            context,
            directory = "light",
            objFileName = "light_cube.obj",
            mtlFileName = "light_cube.mtl"
        )
        model.bind(program, DefaultProgramLocations.resolve(program))

        //val material = model.getPrimaryMaterial()
/*        program.setFloat3("pointLight.position", Vector3(1.0f, 1.0f, 1.0f))
        program.setFloat3("pointLight.ambient", material.ambientColor)
        program.setFloat3("pointLight.diffuse", material.diffuseColor)
        program.setFloat3("pointLight.specular", material.specularColor)
        program.setFloat("pointLight.constant", 1.0f)
        program.setFloat("pointLight.linear", 0.09f)
        program.setFloat("pointLight.quadratic", 0.032f)*/
    }

    // Called for each redraw of the view
    override fun onDrawFrame(unused: GL10?) {
        timer.tick()

        glClearColor(112/255f, 128/255f, 144/255f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)


        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, scaleFactor, scaleFactor, scaleFactor)
        Matrix.rotateM(modelMatrix, 0, rotationX, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f)
        //program.setFloat3("viewPos", camera.position)

        program.use()
        program.setMat4("u_View", camera.getViewMatrix())
        program.setMat4("u_Model", modelMatrix)
        program.setMat4("u_Projection", projectionMatrix)
        model.draw()
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        this.width = width
        this.height = height

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f,10f)
    }
}


