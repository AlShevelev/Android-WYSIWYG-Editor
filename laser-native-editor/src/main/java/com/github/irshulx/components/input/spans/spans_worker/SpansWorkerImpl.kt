package com.github.irshulx.components.input.spans.spans_worker

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.CharacterStyle
import kotlin.reflect.KClass

class SpansWorkerImpl(text: CharSequence?) : SpansWorker {
    private val spannableString = text as SpannableStringBuilder

    @Suppress("UNCHECKED_CAST")
    override fun <T: CharacterStyle>getSpans(spanType: KClass<*>): List<T> =
        getSpansInternal(spanType, 0..spannableString.length)
            .map { it as T }

    @Suppress("UNCHECKED_CAST")
    override fun <T: CharacterStyle>getSpans(spanType: KClass<*>, interval: IntRange): List<T> =
        getSpansInternal(spanType, interval)
            .map { it as T }

    @Suppress("UNCHECKED_CAST")
    override fun <T: CharacterStyle>getSpansWithIntervals(spanType: KClass<*>): List<SpanWithRange<T>> =
            getSpansInternal(spanType, 0..spannableString.length)
                .map {
                    SpanWithRange(it as T, spannableString.getSpanStart(it)..spannableString.getSpanEnd(it))
                }

    override fun removeSpan(span: CharacterStyle) = spannableString.removeSpan(span)

    private fun getSpansInternal(spanType: KClass<*>, interval: IntRange) =
        spannableString.getSpans(interval.first, interval.last, spanType.java)

    override fun createSpan(span: CharacterStyle, interval: IntRange) =
        spannableString.setSpan(span, interval.first, interval.last, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

    /**
     * @return null - a span is not found
     */
    override fun getSpanUnderPosition(spanType: KClass<*>, cursorPosition: Int): CharacterStyle? =
        spannableString.getSpans(cursorPosition, cursorPosition, spanType.java).firstOrNull() as? CharacterStyle

    override fun getSpanInterval(span: CharacterStyle): IntRange =
            spannableString.getSpanStart(span)..spannableString.getSpanEnd(span)
}