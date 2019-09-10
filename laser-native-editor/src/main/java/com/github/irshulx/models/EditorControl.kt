package com.github.irshulx.models

class EditorControl {
    var type: EditorType? = null
    var path: String? = null
    var cords: String? = null
    var textSettings: TextSettings? = null
    var editorTextStyles: MutableList<EditorTextStyle>? = null
    var macroSettings: MutableMap<String, Any>? = null
    var macroName: String? = null
}
