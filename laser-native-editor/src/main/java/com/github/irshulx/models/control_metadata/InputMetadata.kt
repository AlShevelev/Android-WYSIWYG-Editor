package com.github.irshulx.models.control_metadata

import com.github.irshulx.models.EditorTextStyle
import com.github.irshulx.models.EditorType
import com.github.irshulx.models.TextSettings

/**
 * Metadata for text paragraph
 */
data class InputMetadata (
    override val type: EditorType = EditorType.INPUT,
    var textSettings: TextSettings? = null,
    var editorTextStyles: MutableList<EditorTextStyle> = mutableListOf()
) : ControlMetadata