package com.github.irshulx.components.input.spans

import com.github.irshulx.models.EditorTextStyle
import com.github.irshulx.utilities.IdUtil
import java.lang.UnsupportedOperationException

class StyleSpansCollection : SpansCollection<EditorTextStyle>() {
    fun addBold(range: IntRange) = add(create(range, EditorTextStyle.BOLD))

    fun addItalic(range: IntRange) = add(create(range, EditorTextStyle.ITALIC))

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

    override fun Span<EditorTextStyle>.copy(newValue: EditorTextStyle): Span<EditorTextStyle> = StyleSpan(id, range, newValue)

    override fun create(range: IntRange, newValue: EditorTextStyle): Span<EditorTextStyle> =
        StyleSpan(IdUtil.generateLongId(), range, newValue)
}
