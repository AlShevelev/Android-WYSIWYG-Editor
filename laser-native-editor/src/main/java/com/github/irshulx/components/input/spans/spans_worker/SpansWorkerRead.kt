package com.github.irshulx.components.input.spans.spans_worker

import android.text.style.CharacterStyle
import kotlin.reflect.KClass

interface SpansWorkerRead {
    fun <T: CharacterStyle>getSpans(spanType: KClass<*>): List<T>

    fun <T: CharacterStyle>getSpans(spanType: KClass<*>, interval: IntRange): List<T>

    fun <T: CharacterStyle>getSpansWithIntervals(spanType: KClass<*>): List<SpanWithRange<T>>
}