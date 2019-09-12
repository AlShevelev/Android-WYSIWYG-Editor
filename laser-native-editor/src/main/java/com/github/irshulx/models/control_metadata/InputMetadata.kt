package com.github.irshulx.models.control_metadata

import com.github.irshulx.components.input.spans.ColorSpansCollection
import com.github.irshulx.components.input.spans.StyleSpansCollection
import com.github.irshulx.models.EditorType

/**
 * Metadata for text paragraph
 */
data class InputMetadata (
    override val type: EditorType = EditorType.INPUT,
    var colorSpans: ColorSpansCollection = ColorSpansCollection(),
    var styleSpans: StyleSpansCollection = StyleSpansCollection()
) : ControlMetadata