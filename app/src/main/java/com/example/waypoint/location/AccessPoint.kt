package com.example.waypoint.location

import android.content.Context
import com.example.waypoint.loadJsonFromAssets
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class AccessPoint(
    val ssid: String,
    val bssid: String,
    val xPos: Double,
    val yPos: Double,
    val referenceRSSI: Int,
    val environmentalFactor: Double,
)

data class LocationData(
    @SerializedName("access-points") val accessPoints: List<AccessPoint>,
)

fun parseJsonWithGson(
    context: Context,
    fileName: String,
): LocationData {
    val json = loadJsonFromAssets(context, fileName)
    val gson = Gson()
    return gson.fromJson(json, LocationData::class.java)
}
