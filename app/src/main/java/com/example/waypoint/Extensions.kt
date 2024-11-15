package com.example.waypoint

import android.content.Context
import android.content.res.Resources
import androidx.annotation.RawRes
import java.io.BufferedReader
import java.io.InputStreamReader

fun loadJsonFromAssets(
    context: Context,
    fileName: String,
): String {
    val inputStream = context.assets.open(fileName)
    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
    return bufferedReader.use { it.readText() }
}

fun Resources.readRawTextFile(
    @RawRes id: Int,
) = openRawResource(id).bufferedReader().use { it.readText() }
