package com.github.irshulx.models.control_metadata

import com.github.irshulx.models.EditorType

data class ImageMetadata (
    override val type: EditorType,
    var path: String? = null,
    var cords: String? = null
) : ControlMetadata
