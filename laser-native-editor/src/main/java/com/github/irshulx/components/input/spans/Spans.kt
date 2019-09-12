package com.github.irshulx.components.input.spans

import com.github.irshulx.models.EditorTextStyle

abstract class Span<T> (val id: Long, val area: IntRange, val value: T)

class StyleSpan(id: Long, area: IntRange, value: EditorTextStyle): Span<EditorTextStyle>(id, area, value)

/**
 * [value] - color's value as Int
 */
class ColorSpan(id: Long, area: IntRange, value: Int): Span<Int>(id, area, value)