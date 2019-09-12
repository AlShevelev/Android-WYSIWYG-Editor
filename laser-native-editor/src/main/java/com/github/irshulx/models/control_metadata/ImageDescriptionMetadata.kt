package com.github.irshulx.models.control_metadata

import com.github.irshulx.models.EditorTextStyle
import com.github.irshulx.models.EditorType
import com.github.irshulx.models.TextSettings

/**
 * Metadata for an image description
 */
data class ImageDescriptionMetadata (
    override val type: EditorType = EditorType.IMG_SUB,
    var textSettings: TextSettings? = null,
    var editorTextStyles: MutableList<EditorTextStyle> = mutableListOf()
) : ControlMetadata