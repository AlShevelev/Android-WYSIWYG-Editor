package com.github.irshulx.components.input.edit_text

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.google.android.material.textfield.TextInputEditText

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

    val cursorPosition: Int
        get() = selectionStart

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection =
            CustomInputConnection(this, super.onCreateInputConnection(outAttrs), true)

    fun setCursorPosition(position: Int) = setSelection(position)
}