package com.github.irshulx.utilities

import android.content.Context
import android.widget.Toast

object Utilities {
    fun toastItOut(context: Context, message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    fun dpToPx(context: Context, dp: Float): Int {
        val metrics = context.resources.displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return px.toInt()
    }
}