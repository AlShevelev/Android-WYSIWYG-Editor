package com.github.irshulx

import android.graphics.Bitmap
import android.text.Editable
import android.view.View
import android.widget.EditText

interface EditorListener {
    fun onTextChanged(editText: EditText, text: Editable)
    fun onUpload(image: Bitmap, uuid: String)
    fun onRenderMacro(name: String, props: Map<String, Any>, index: Int): View
}