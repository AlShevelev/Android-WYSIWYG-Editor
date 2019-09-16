package com.github.irshulx.components.input.spans.calculators

import android.text.style.CharacterStyle
import com.github.irshulx.components.input.spans.spans_worker.SpansWorkerRead
import kotlin.reflect.KClass

@Suppress("LeakingThis")
abstract class SpansCalculator<T>(spansReader: SpansWorkerRead) {
    private val spans: List<SpanInfo<T>>
    private val spansInText: Map<SpanInfo<T>, CharacterStyle>

    init {
        // Spans list filling
        val spans = mutableListOf<SpanInfo<T>>()
        val spansInText = mutableMapOf<SpanInfo<T>, CharacterStyle>()

        spansReader.getSpansWithIntervals<CharacterStyle>(getSpanClass())
            .map {
                createSpanInfo(it.spanInterval, getSpanValue(it.span))
            }

        this.spans = spans
        this.spansInText = spansInText
    }

    protected fun calculate(span: SpanInfo<T>): List<SpanOperation> {
        if(span.area.first >= span.area.last) {
            return listOf()
        }

        if(spans.isEmpty()) {
            return listOf(CreateSpanOperation(span))
        }

        return calculateOperations(span, calculateIntersections(span))
    }

    /**
     * @return null - no span needed
     */
    protected abstract fun calculateSpanValue(oldValue: T, newValue: T): T?

    /**
     * @return null - no span needed
     */
    protected abstract fun SpanInfo<T>.copy(newValue: T): SpanInfo<T>

    protected abstract fun createSpanInfo(area: IntRange, newValue: T): SpanInfo<T>

    protected abstract fun getSpanClass(): KClass<*>

    protected abstract fun getSpanValue(rawSpan: Any): T

    private fun calculateIntersections(span: SpanInfo<T>): SpansIntersections<T> {
        val intersections = SpansIntersections<T>()

        spans.forEach {
            when {
                it.area.first == span.area.first && it.area.last == span.area.last ->
                    intersections.spansIntersectFull.add(it)

                it.area.first >= span.area.first && it.area.last <= span.area.last ->
                    intersections.spansInsideFull.add(it)

                it.area.first >= span.area.first && it.area.last <= span.area.last ->
                    intersections.spansInsideFull.add(it)

                it.area.first < span.area.first && it.area.last >= span.area.first ->
                    intersections.spansInsideLeft.add(it)

                it.area.last > span.area.last && it.area.first <= span.area.last ->
                    intersections.spansInsideRight.add(it)

                it.area.first < span.area.first && it.area.last > span.area.last ->
                    intersections.spansOutsideFull.add(it)
            }
        }

        return intersections
    }

    private fun calculateOperations(span: SpanInfo<T>, intersections: SpansIntersections<T>): List<SpanOperation> {
        val result = mutableListOf<SpanOperation>()

        // Without intersections
        if(!intersections.hasIntersections()) {
            result.add(CreateSpanOperation(span))
            return result
        }

        // Has full intersection
        val fullIntersection = intersections.spansIntersectFull.firstOrNull()
        if(fullIntersection != null) {
            val newSpanValue = calculateSpanValue(fullIntersection.value, span.value)
            if(fullIntersection.value == newSpanValue) {
                return result
            }

            result.add(DeleteSpanOperation(spansInText.getValue(fullIntersection)))

            newSpanValue?.let {
                span.copy(it).let { newSpan ->
                    result.add(CreateSpanOperation(newSpan))
                }
            }

            return result
        }

        // Has old full-outside span
        val oldFullOutside = intersections.spansOutsideFull.firstOrNull()
        if(oldFullOutside != null) {
            val newSpanValue = calculateSpanValue(oldFullOutside.value, span.value)
            if(oldFullOutside.value == newSpanValue) {
                return result
            }

            result.add(DeleteSpanOperation(spansInText.getValue(oldFullOutside)))

            val newLeftSpan = createSpanInfo(oldFullOutside.area.first..span.area.first, oldFullOutside.value)
            result.add(CreateSpanOperation(newLeftSpan))

            val newRightSpan = createSpanInfo(span.area.last..oldFullOutside.area.last, oldFullOutside.value)
            result.add(CreateSpanOperation(newRightSpan))

            newSpanValue?.let {
                span.copy(it).let { newSpan ->
                    result.add(CreateSpanOperation(newSpan))
                }
            }

            return result
        }

        // Remove full inside spans
        intersections.spansInsideFull.forEach { insideSpan ->
            result.add(DeleteSpanOperation(spansInText.getValue(insideSpan)))
        }

        // Process left-intersected spans
        intersections.spansInsideLeft.forEach { oldLeftSpan ->
            createSpanInfo(oldLeftSpan.area.first..span.area.first, oldLeftSpan.value).let { newLeftSpan ->
                result.add(DeleteSpanOperation(spansInText.getValue(oldLeftSpan)))

                result.add(CreateSpanOperation(newLeftSpan))

            }
        }

        // Process right-intersected spans
        intersections.spansInsideRight.forEach { oldRightSpan ->
            createSpanInfo(span.area.last..oldRightSpan.area.last, oldRightSpan.value).let { newRightSpan ->
                result.add(DeleteSpanOperation(spansInText.getValue(oldRightSpan)))

                result.add(CreateSpanOperation(newRightSpan))

            }
        }

        // Add new span
        result.add(CreateSpanOperation(span))

        return result
    }
}