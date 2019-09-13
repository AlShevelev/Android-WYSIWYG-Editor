package com.github.irshulx.wysiwyg.Utilities

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.EditText

/**
 * Created by mkallingal on 1/31/2016.
 */
class MyTextWatcher//constructor
(var editText: EditText) : TextWatcher {
    init {
        //Code for monitoring keystrokes
        editText.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                editText.setText("yippe k")
            }
            false
        }
    }

    //Some manipulation with text
    override fun afterTextChanged(s: Editable) {
        if (editText.text.length == 12) {
            editText.text = editText.text.delete(editText.text.length - 1, editText.text.length)
            editText.setSelection(editText.text.toString().length)
        }
        if (editText.text.length == 2 || editText.text.length == 5 || editText.text.length == 8) {
            editText.setText(editText.text.toString() + "/")
            editText.setSelection(editText.text.toString().length)
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {


    }
}