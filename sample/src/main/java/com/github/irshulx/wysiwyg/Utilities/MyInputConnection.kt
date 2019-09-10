package com.github.irshulx.wysiwyg.Utilities

import android.text.Editable
import android.text.SpannableStringBuilder
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.widget.TextView

/**
 * Created by mkallingal on 1/17/2016.
 */
internal class MyInputConnection(targetView: View, fullEditor: Boolean) : BaseInputConnection(targetView, fullEditor) {
    private var _editable: SpannableStringBuilder? = null
    var _textView: TextView

    init {
        _textView = targetView as TextView
    }

    override fun getEditable(): Editable {
        if (_editable == null) {
            _editable = Editable.Factory.getInstance()
                    .newEditable("Placeholder") as SpannableStringBuilder
        }
        return _editable!!
    }

    override fun commitText(text: CharSequence, newCursorPosition: Int): Boolean {
        _editable!!.append(text)
        _textView.text = text
        return true
    }
}