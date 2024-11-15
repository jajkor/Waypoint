package com.example.waypoint.renderer.scene

import com.example.waypoint.Vector3

data class Light(
    val lightColor: Vector3 = Vector3(1.0f, 1.0f, 1.0f),
    var lightPosition: Vector3 = Vector3(0.0f, 0.0f, 0.0f),
)
