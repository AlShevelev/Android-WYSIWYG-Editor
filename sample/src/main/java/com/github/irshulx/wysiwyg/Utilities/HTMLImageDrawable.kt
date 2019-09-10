package com.github.irshulx.wysiwyg.Utilities

import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

class HTMLImageDrawable : BitmapDrawable() {

    var drawable: Drawable? = null

    override fun draw(canvas: Canvas) {
        if (drawable != null) {
            drawable!!.draw(canvas)
        }
    }
}
