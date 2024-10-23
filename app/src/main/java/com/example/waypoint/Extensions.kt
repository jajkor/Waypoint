package com.example.waypoint

import android.content.res.Resources
import androidx.annotation.RawRes

fun Resources.readRawTextFile(
    @RawRes id: Int,
) = openRawResource(id).bufferedReader().use { it.readText() }
