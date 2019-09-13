package com.github.irshulx.components.input.edit_text

import android.annotation.SuppressLint
import android.content.Context
import android.support.design.widget.TextInputEditText
import android.util.AttributeSet
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

    val selectionArea: IntRange?
        get() = if(selectionStart != selectionEnd) selectionStart .. selectionEnd else null

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection =
            CustomInputConnection(this, super.onCreateInputConnection(outAttrs), true)
}