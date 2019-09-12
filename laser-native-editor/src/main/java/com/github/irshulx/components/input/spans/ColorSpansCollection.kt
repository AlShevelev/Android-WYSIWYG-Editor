package com.github.irshulx.components.input.spans

import com.github.irshulx.utilities.IdUtil

class ColorSpansCollection: SpansCollection<Int>() {
    fun add(area: IntRange, color: Int) = add(create(area, color))

    override fun calculateSpanValue(oldValue: Int, newValue: Int): Int? = newValue

    override fun Span<Int>.copy(newValue: Int): Span<Int> = ColorSpan(id, area, newValue)

    override fun create(area: IntRange, newValue: Int): Span<Int> = ColorSpan(IdUtil.generateLongId(), area, newValue)
}