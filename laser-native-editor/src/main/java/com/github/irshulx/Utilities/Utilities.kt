package com.github.irshulx.Utilities

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.util.Size
import android.widget.Toast

import java.util.regex.Pattern

object Utilities {
    fun getScreenDimension(context: Context): Size {
        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return Size(size.x, size.y)
    }

    fun toastItOut(context: Context, message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    fun containsString(text: String): Boolean {
        val htmlPattern = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>"
        val pattern = Pattern.compile(htmlPattern)
        val matcher = pattern.matcher(text)
        return matcher.matches()
    }

    fun dpToPx(context: Context, dp: Float): Int {
        val metrics = context.resources.displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return px.toInt()
    }

    fun pxToDp(context: Context, px: Float): Int {
        return (px / context.resources.displayMetrics.density).toInt()
    }
}