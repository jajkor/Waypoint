package com.example.waypoint.location

import com.example.waypoint.Vector2

data class AccessPoint(
    val ssid: String,
    val bssid: String,
    val position: Vector2,
    val referenceRSSI: Int,
    val environmentalFactor: Double
) {
    companion object {
        fun fromJson(json: Map<String, Any>): AccessPoint =
            AccessPoint(
                ssid = json["ssid"] as String,
                bssid = json["bssid"] as String,
                position =
                Vector2(
                    (json["xPos"] as Number).toFloat(),
                    (json["yPos"] as Number).toFloat()
                ),
                referenceRSSI = (json["referenceRSSI"] as Number).toInt(),
                environmentalFactor = (json["environmentalFactor"] as Number).toDouble()
            )
    }
}
