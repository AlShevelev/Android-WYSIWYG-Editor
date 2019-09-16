package com.github.irshulx.models.control_metadata

import com.github.irshulx.models.EditorType

/**
 * Metadata for text paragraph
 */
data class InputMetadata (
        override val type: EditorType = EditorType.INPUT
) : ControlMetadata