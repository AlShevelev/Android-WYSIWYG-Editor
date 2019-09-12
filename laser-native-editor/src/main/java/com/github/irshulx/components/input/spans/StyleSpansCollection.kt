package com.github.irshulx.components.input.spans

import com.github.irshulx.models.EditorTextStyle
import com.github.irshulx.utilities.IdUtil
import java.lang.UnsupportedOperationException

class StyleSpansCollection : SpansCollection<EditorTextStyle>() {
    fun add(area: IntRange, style: EditorTextStyle) =
        if(style == EditorTextStyle.BOLD || style == EditorTextStyle.ITALIC) {
            add(create(area, style))
        } else {
            throw UnsupportedOperationException("This style is not supported: $style")
        }

    override fun calculateSpanValue(oldValue: EditorTextStyle, newValue: EditorTextStyle): EditorTextStyle? {
        return when (newValue) {
            EditorTextStyle.BOLD -> {
                when (oldValue) {
                    EditorTextStyle.BOLD -> null
                    EditorTextStyle.ITALIC -> EditorTextStyle.BOLD_ITALIC
                    EditorTextStyle.BOLD_ITALIC -> EditorTextStyle.ITALIC
                    else -> throw UnsupportedOperationException("This style is not supported: $oldValue")
                }
            }
            EditorTextStyle.ITALIC -> {
                when (oldValue) {
                    EditorTextStyle.BOLD -> EditorTextStyle.BOLD_ITALIC
                    EditorTextStyle.ITALIC -> null
                    EditorTextStyle.BOLD_ITALIC -> EditorTextStyle.BOLD
                    else -> throw UnsupportedOperationException("This style is not supported: $oldValue")
                }
            }
            else -> throw UnsupportedOperationException("This style is not supported: $newValue")
        }
    }

    override fun Span<EditorTextStyle>.copy(newValue: EditorTextStyle): Span<EditorTextStyle> = StyleSpan(id, area, newValue)

    override fun create(area: IntRange, newValue: EditorTextStyle): Span<EditorTextStyle> =
        StyleSpan(IdUtil.generateLongId(), area, newValue)
}
