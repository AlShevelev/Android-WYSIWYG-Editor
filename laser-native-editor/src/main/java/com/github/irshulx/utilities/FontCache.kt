package com.github.irshulx.utilities

import android.content.Context
import android.graphics.Typeface

object FontCache {
    private val fontCache = mutableMapOf<String, Typeface>()

    operator fun get(name: String, context: Context): Typeface? = fontCache[name] ?: createTypeFace(name, context)

    private fun createTypeFace(name: String, context: Context): Typeface? {
        val tf = try {
            Typeface.createFromAsset(context.assets, name)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }

        fontCache[name] = tf
        return tf
    }
}