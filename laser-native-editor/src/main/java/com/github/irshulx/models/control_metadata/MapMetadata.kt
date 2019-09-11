package com.github.irshulx.models.control_metadata

import com.github.irshulx.models.EditorType

data class MapMetadata (
    override val type: EditorType = EditorType.MACRO,
    var cords: String
) : ControlMetadata
