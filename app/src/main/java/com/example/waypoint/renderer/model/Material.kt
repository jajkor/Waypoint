package com.example.waypoint.renderer.model

import com.example.waypoint.Vector3

data class Material(
    val ambientColor: Vector3,
    val diffuseColor: Vector3,
    val specularColor: Vector3,
    val specularComponent: Float
)
