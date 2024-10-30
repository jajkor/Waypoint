package com.example.waypoint.renderer

import android.content.Context
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_FLOAT_MAT4
import android.opengl.GLES20.GL_FLOAT_VEC3
import android.opengl.GLES32.GL_BLEND
import android.opengl.GLES32.GL_COLOR_BUFFER_BIT
import android.opengl.GLES32.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES32.GL_DEPTH_TEST
import android.opengl.GLES32.GL_MAJOR_VERSION
import android.opengl.GLES32.GL_MINOR_VERSION
import android.opengl.GLES32.GL_ONE_MINUS_SRC_ALPHA
import android.opengl.GLES32.GL_SRC_ALPHA
import android.opengl.GLES32.glBlendFunc
import android.opengl.GLES32.glClear
import android.opengl.GLES32.glClearColor
import android.opengl.GLES32.glEnable
import android.opengl.GLES32.glGetIntegerv
import android.opengl.GLES32.glViewport
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.waypoint.ModelLoader
import com.example.waypoint.R
import com.example.waypoint.readRawTextFile
import com.example.waypoint.renderer.model.Model
import com.example.waypoint.renderer.model.Uniform
import com.example.waypoint.renderer.scene.Camera
import com.example.waypoint.renderer.scene.Light
import com.example.waypoint.renderer.scene.Timer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MapRenderer(
    private val context: Context,
    private val camera: Camera,
) : GLSurfaceView.Renderer {
    private lateinit var campusModel: Model
    private lateinit var pathModel: Model
    private lateinit var gridQuad: Model

    private lateinit var gridShader: Program
    private lateinit var campusShader: Program
    private lateinit var pathShader: Program
    private lateinit var displayNormalsShader: Program

    private var timer: Timer = Timer()
    private var globalLight: Light = Light()

    private val backgroundColor: Vector3 = Vector3(0.949f, 0.949f, 0.949f)

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
        glCheckVersion()

        glEnable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        gridShader =
            Program(
                context.resources.readRawTextFile(R.raw.grid_vert),
                context.resources.readRawTextFile(R.raw.grid_frag),
            )
        gridQuad = ModelLoader(context).loadModel("grid/grid.obj", "grid/grid.mtl")

        campusShader =
            Program(
                context.resources.readRawTextFile(R.raw.campus_vert),
                context.resources.readRawTextFile(R.raw.campus_frag),
            )
        campusModel = ModelLoader(context).loadModel("campus/sutherland_f1.obj", "campus/sutherland_f1.mtl")

        pathShader =
            Program(
                context.resources.readRawTextFile(R.raw.path_vert),
                context.resources.readRawTextFile(R.raw.path_frag),
                context.resources.readRawTextFile(R.raw.path_geom),
            )
        pathModel = ModelLoader(context).loadModel("path/path.obj", "path/path.mtl")

        displayNormalsShader =
            Program(
                context.resources.readRawTextFile(R.raw.display_normals_vert),
                context.resources.readRawTextFile(R.raw.display_normals_frag),
                context.resources.readRawTextFile(R.raw.display_normals_geom),
            )

        globalLight.lightPosition = Vector3(30.0f, 60f, 30.0f)
    }

    // Called for each redraw of the view
    override fun onDrawFrame(unused: GL10?) {
        glClearColor(backgroundColor.x, backgroundColor.y, backgroundColor.z, 1.0f) // Set the background frame colors
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        viewMatrix = camera.getViewMatrix()

        val paths = mutableListOf(Vector3(0.000f, 1.000f, 0.000f), Vector3(10.000f, 1.000f, 0.000f))
        drawPath(paths)

        val uniforms =
            listOf(
                Uniform("u_Model", GL_FLOAT_MAT4, modelMatrix),
                Uniform("u_View", GL_FLOAT_MAT4, viewMatrix),
                Uniform("u_Projection", GL_FLOAT_MAT4, projectionMatrix),
                Uniform("viewPos", GL_FLOAT_VEC3, camera.getPosition()),
                Uniform("lightColor", GL_FLOAT_VEC3, globalLight.lightColor),
                Uniform("lightPos", GL_FLOAT_VEC3, globalLight.lightPosition),
                Uniform("ambientColor", GL_FLOAT_VEC3, campusModel.getMaterial().ambientColor),
                Uniform("diffuseColor", GL_FLOAT_VEC3, campusModel.getMaterial().diffuseColor),
                Uniform("specularColor", GL_FLOAT_VEC3, campusModel.getMaterial().specularColor),
                Uniform("specularComponent", GL_FLOAT, campusModel.getMaterial().specularComponent),
            )
        drawModel(gridQuad, gridShader, false, uniforms)
        drawModel(campusModel, campusShader, false, uniforms, Vector3(10f, 5f, 10f), Vector3(0.0f, 0.1f, 0.0f))
    }

    private fun drawPath(paths: List<Vector3>) {
        pathShader.use()
        Matrix.setIdentityM(modelMatrix, 0)
        pathShader.setMat4("u_View", viewMatrix)
        pathShader.setMat4("u_Projection", projectionMatrix)
        pathShader.setFloat("u_Time", timer.sinceLastFrameSecs()) // https://thebookofshaders.com/03/
        pathShader.setVector3("u_UserPos", Vector3(0.000f, 1.000f, 0.000f))
        pathShader.setVector3("u_NodePos", Vector3(10.000f, 1.000f, 0.000f))

        pathModel.drawPoints()
    }

    private fun drawModel(
        model: Model,
        program: Program,
        drawNorms: Boolean,
        uniforms: List<Uniform>,
        scale: Vector3? = null,
        translation: Vector3? = null,
    ) {
        if (drawNorms) {
            drawModel(model, displayNormalsShader, false, uniforms, scale, translation)
        }

        program.use()
        Matrix.setIdentityM(modelMatrix, 0)
        if (scale != null) {
            Matrix.scaleM(modelMatrix, 0, scale.x, scale.y, scale.z)
        }
        if (translation != null) {
            Matrix.translateM(modelMatrix, 0, translation.x, translation.y, translation.z)
        }
        for (uniform in uniforms) {
            when (uniform.type) {
                GL_FLOAT -> program.setFloat(uniform.name, (uniform.value as Number).toFloat())
                GL_FLOAT_VEC3 -> program.setVector3(uniform.name, (uniform.value as Vector3))
                GL_FLOAT_MAT4 -> program.setMat4(uniform.name, (uniform.value as FloatArray))
            }
        }

        model.draw()
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

    private fun glCheckVersion() {
        val glVersion = IntArray(2)
        glGetIntegerv(GL_MAJOR_VERSION, glVersion, 0)
        glGetIntegerv(GL_MINOR_VERSION, glVersion, 1)

        if (glVersion[0] >= 3 && glVersion[1] >= 2) {
            // Set up for OpenGL ES 3.2 specific features (like geometry shaders)
            Log.i("OpenGL", "OpenGL ES 3.2 supported")
        } else {
            throw RuntimeException("OpenGL ES 3.2 is not supported on this device")
        }
    }
}
