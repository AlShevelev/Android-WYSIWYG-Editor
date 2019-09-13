package com.github.irshulx.models

class Node {
    var type: EditorType? = null
    var content: MutableList<String>? = null

    var textSettings: TextSettings? = null
    var editorTextStyles: MutableList<EditorTextStyle> = mutableListOf()

    var childs: MutableList<Node>? = null
    var macroSettings: MutableMap<String, Any>? = null
}
