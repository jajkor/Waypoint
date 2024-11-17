package com.example.waypoint

import android.content.res.Resources
import androidx.annotation.RawRes
import org.json.JSONArray
import org.json.JSONObject

fun Resources.readRawTextFile(
    @RawRes id: Int,
) = openRawResource(id).bufferedReader().use { it.readText() }

fun JSONObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    val keys = this.keys()

    while (keys.hasNext()) {
        val key = keys.next()
        var value: Any = this.get(key)

        value =
            when (value) {
                is JSONObject -> value.toMap()
                is JSONArray -> value.toList()
                else -> value
            }

        map[key] = value
    }

    return map
}

private fun JSONArray.toList(): List<Any> {
    val list = mutableListOf<Any>()
    for (i in 0 until this.length()) {
        var value: Any = this.get(i)

        value =
            when (value) {
                is JSONObject -> value.toMap()
                is JSONArray -> value.toList()
                else -> value
            }

        list.add(value)
    }
    return list
}
