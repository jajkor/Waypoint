package com.example.waypoint.model

import Vector3
import com.example.waypoint.Texture

data class Material(
    val ambientColor: Vector3,
    val diffuseColor: Vector3,
    val specularColor: Vector3,
    val diffuseTexture: Texture?,
    val specularTexture: Texture?
)