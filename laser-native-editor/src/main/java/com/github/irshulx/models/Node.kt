package com.github.irshulx.models

import com.github.irshulx.components.input.spans.ColorSpansCollection
import com.github.irshulx.components.input.spans.StyleSpansCollection

class Node {
    var type: EditorType? = null
    var content: MutableList<String>? = null

    var styleSpans: StyleSpansCollection = StyleSpansCollection()
    var colorSpans: ColorSpansCollection = ColorSpansCollection()

    var textSettings: TextSettings? = null
    var editorTextStyles: MutableList<EditorTextStyle> = mutableListOf()

    var childs: MutableList<Node>? = null
    var macroSettings: MutableMap<String, Any>? = null
}
