package com.github.irshulx.components.input.edit_text

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.support.design.widget.TextInputEditText
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

@SuppressLint("ClickableViewAccessibility")
class CustomEditText
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : TextInputEditText(context, attrs, defStyle) {

    private var lastTouchPoint: PointF? = null

    val selectionArea: IntRange?
        get() = if(selectionStart != selectionEnd) selectionStart .. selectionEnd else null

    init {
        // Store last touch point
        this.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_UP) {
                lastTouchPoint = PointF(event.x, event.y)
            }
            false
        }
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection =
            CustomInputConnection(this, super.onCreateInputConnection(outAttrs), true)

    /**
     * Copy/paste floating menu restoration
     */
    fun restoreFloatingMenu(selection: IntRange) {
        if(selection.first != selection.last) {
            lastTouchPoint?.let {
                val pause = 450L    // in [ms]
                val now = SystemClock.uptimeMillis()

                Handler(Looper.getMainLooper()).post {
                    dispatchTouchEvent(MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, it.x, it.y, 0))
                    setSelection(selection.first, selection.last)
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    dispatchTouchEvent(MotionEvent.obtain(now + pause, now + pause, MotionEvent.ACTION_UP, it.x, it.y, 0))
                }, pause)
            }
        }
    }
}