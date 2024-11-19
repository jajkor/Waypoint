package com.example.waypoint.renderer

import android.content.Context
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_FLOAT_MAT4
import android.opengl.GLES20.GL_FLOAT_VEC3
import android.opengl.GLES30.GL_BLEND
import android.opengl.GLES30.GL_COLOR_BUFFER_BIT
import android.opengl.GLES30.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES30.GL_DEPTH_TEST
import android.opengl.GLES30.GL_ONE_MINUS_SRC_ALPHA
import android.opengl.GLES30.GL_SRC_ALPHA
import android.opengl.GLES30.GL_TRIANGLES
import android.opengl.GLES30.glBlendFunc
import android.opengl.GLES30.glClear
import android.opengl.GLES30.glClearColor
import android.opengl.GLES30.glEnable
import android.opengl.GLES30.glViewport
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.waypoint.Vector3
import com.example.waypoint.readRawTextFile
import com.example.waypoint.renderer.model.Model
import com.example.waypoint.renderer.model.ModelLoader
import com.example.waypoint.renderer.model.Uniform
import com.example.waypoint.renderer.scene.Camera
import com.example.waypoint.renderer.scene.Light
import com.sarimkazmi.guideme.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MapRenderer(
    private val context: Context,
    private val camera: Camera
) : GLSurfaceView.Renderer {
    private lateinit var campusModel: Model
    private lateinit var userModel: Model
    private lateinit var gridQuad: Model

    private lateinit var gridShader: Program
    private lateinit var campusShader: Program

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
        config: EGLConfig
    ) {
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        gridShader =
            Program(
                context.resources.readRawTextFile(R.raw.grid_vert),
                context.resources.readRawTextFile(R.raw.grid_frag)
            )
        gridQuad = ModelLoader(context).loadModel("models/grid/grid.obj", "models/grid/grid.mtl")

        campusShader =
            Program(
                context.resources.readRawTextFile(R.raw.campus_vert),
                context.resources.readRawTextFile(R.raw.campus_frag)
            )
        campusModel =
            ModelLoader(
                context
            ).loadModel("campus/rydal_executive_plaza/third_floor/3rdfloor.obj", "campus/rydal_executive_plaza/third_floor/3rdfloor.mtl")

        userModel =
            ModelLoader(
                context
            ).loadModel("models/user/user.obj", "models/user/user.mtl")

        globalLight.lightPosition = Vector3(30.0f, 60f, 30.0f)
    }

    // Called for each redraw of the view
    override fun onDrawFrame(unused: GL10?) {
        glClearColor(backgroundColor.x, backgroundColor.y, backgroundColor.z, 1.0f) // Set the background frame colors
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        viewMatrix = camera.getViewMatrix()

        val campusUniforms =
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
                Uniform("specularComponent", GL_FLOAT, campusModel.getMaterial().specularComponent)
            )
        drawModel(gridQuad, gridShader, campusUniforms, GL_TRIANGLES)
        drawModel(campusModel, campusShader, campusUniforms, GL_TRIANGLES, Vector3(10f, 3f, 10f), Vector3(0.0f, 0.1f, 0.0f))

        val userUniforms =
            listOf(
                Uniform("u_Model", GL_FLOAT_MAT4, modelMatrix),
                Uniform("u_View", GL_FLOAT_MAT4, viewMatrix),
                Uniform("u_Projection", GL_FLOAT_MAT4, projectionMatrix),
                Uniform("viewPos", GL_FLOAT_VEC3, camera.getPosition()),
                Uniform("lightColor", GL_FLOAT_VEC3, globalLight.lightColor),
                Uniform("lightPos", GL_FLOAT_VEC3, globalLight.lightPosition),
                Uniform("ambientColor", GL_FLOAT_VEC3, userModel.getMaterial().ambientColor),
                Uniform("diffuseColor", GL_FLOAT_VEC3, userModel.getMaterial().diffuseColor),
                Uniform("specularColor", GL_FLOAT_VEC3, userModel.getMaterial().specularColor),
                Uniform("specularComponent", GL_FLOAT, userModel.getMaterial().specularComponent)
            )

        drawModel(
            userModel,
            campusShader,
            userUniforms,
            GL_TRIANGLES,
            Vector3(1f, 1f, 1f),
            Vector3(camera.getPivot().x, 1f, camera.getPivot().z)
        )
    }

    private fun drawModel(
        model: Model,
        program: Program,
        uniforms: List<Uniform>,
        primitiveType: Int,
        scale: Vector3? = null,
        translation: Vector3? = null
    ) {
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

        model.draw(primitiveType)
    }

    fun updateCameraPivot(newPivot: Vector3) {
        camera.setPivot(newPivot)
    }

    fun setPitch(pitch: Float) {
        camera.setPitch(pitch)
    }

    fun setFreeLook(freeLook: Boolean) {
        camera.setFreeLook(freeLook)
    }

    override fun onSurfaceChanged(
        unused: GL10?,
        width: Int,
        height: Int
    ) {
        glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 100f)
    }
}
