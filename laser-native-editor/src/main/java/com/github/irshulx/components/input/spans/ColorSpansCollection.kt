package com.github.irshulx.components.input.spans

import com.github.irshulx.utilities.IdUtil

class ColorSpansCollection: SpansCollection<Int>() {
    fun add(range: IntRange, color: Int) = add(create(range, color))

    override fun calculateSpanValue(oldValue: Int, newValue: Int): Int? = newValue

    override fun Span<Int>.copy(newValue: Int): Span<Int> = ColorSpan(id, range, newValue)

    override fun create(range: IntRange, newValue: Int): Span<Int> = ColorSpan(IdUtil.generateLongId(), range, newValue)
}