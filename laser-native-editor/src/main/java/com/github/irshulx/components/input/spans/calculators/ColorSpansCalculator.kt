package com.github.irshulx.components.input.spans.calculators

import android.text.style.ForegroundColorSpan
import com.github.irshulx.components.input.spans.spans_worker.SpansWorkerRead
import kotlin.reflect.KClass

class ColorSpansCalculator(spansReader: SpansWorkerRead): SpansCalculator<Int>(spansReader) {
    fun calculate(area: IntRange, color: Int) = calculate(createSpanInfo(area, color))

    override fun calculateSpanValue(oldValue: Int, newValue: Int): Int? = newValue

    override fun SpanInfo<Int>.copy(newValue: Int): SpanInfo<Int> = ColorSpanInfo(area, newValue)

    override fun createSpanInfo(area: IntRange, newValue: Int): SpanInfo<Int> = ColorSpanInfo(area, newValue)

    override fun getSpanClass(): KClass<*> = ForegroundColorSpan::class

    override fun getSpanValue(rawSpan: Any): Int = (rawSpan as ForegroundColorSpan).foregroundColor
}