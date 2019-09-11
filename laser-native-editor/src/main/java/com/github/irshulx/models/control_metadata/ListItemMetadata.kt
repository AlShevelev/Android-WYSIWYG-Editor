package com.github.irshulx.models.control_metadata

import com.github.irshulx.models.EditorTextStyle
import com.github.irshulx.models.EditorType
import com.github.irshulx.models.TextSettings

/**
 * Metadata for text paragraph
 */
data class ListItemMetadata (
    override val type: EditorType,
    var textSettings: TextSettings? = null,
    var editorTextStyles: MutableList<EditorTextStyle> = mutableListOf()
) : ControlMetadata