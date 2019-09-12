package com.github.irshulx.components.input.spans

import com.github.irshulx.models.EditorTextStyle

abstract class Span<T> (val id: Long, val range: IntRange, val value: T)

class StyleSpan(id: Long, range: IntRange, value: EditorTextStyle): Span<EditorTextStyle>(id, range, value)

/**
 * [value] - color's value as Int
 */
class ColorSpan(id: Long, range: IntRange, value: Int): Span<Int>(id, range, value)