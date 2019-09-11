package com.github.irshulx.models.control_metadata

import com.github.irshulx.models.EditorType

data class MacroMetadata (
    override val type: EditorType = EditorType.MACRO,
    var macroSettings: MutableMap<String, Any>? = mutableMapOf(),
    var macroName: String? = null
) : ControlMetadata
