package com.audiosourcechecker

import android.util.Log


inline fun <reified T> createIntPropertyMapping(): Map<Int, String> {
    val fields = T::class.java.declaredFields
    val returnMap = HashMap<Int, String>()

    for (field in fields) {
        val key: Int = try {
            field.getInt(null)
        } catch (e: Exception) {
            Log.e("MAPPING", "Failed to create key")
            continue
        }

        val name = field.name
        returnMap[key] = name

    }

    return returnMap
}