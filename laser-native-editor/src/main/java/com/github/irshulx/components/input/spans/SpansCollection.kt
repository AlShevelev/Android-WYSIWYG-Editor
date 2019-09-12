package com.github.irshulx.components.input.spans

abstract class SpansCollection<T> {
    private val spans = mutableListOf<Span<T>>()

    protected fun add(span: Span<T>): List<SpanOperation> {
        if(span.range.first >= span.range.last) {
            return listOf()
        }

        if(spans.isEmpty()) {
            spans.add(span)
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
    protected abstract fun Span<T>.copy(newValue: T): Span<T>

    protected abstract fun create(range: IntRange, newValue: T): Span<T>

    private fun calculateIntersections(span: Span<T>): SpansIntersections<T> {
        val intersections = SpansIntersections<T>()

        spans.forEach {
            when {
                it.range.first == span.range.first && it.range.last == span.range.last ->
                    intersections.spansIntersectFull.add(it)

                it.range.first >= span.range.first && it.range.last <= span.range.last ->
                    intersections.spansInsideFull.add(it)

                it.range.first >= span.range.first && it.range.last <= span.range.last ->
                    intersections.spansInsideFull.add(it)

                it.range.first < span.range.first && it.range.last >= span.range.first ->
                    intersections.spansInsideLeft.add(it)

                it.range.last > span.range.last && it.range.first <= span.range.last ->
                    intersections.spansInsideRight.add(it)

                it.range.first < span.range.first && it.range.last > span.range.last ->
                    intersections.spansOutsideFull.add(it)
            }
        }

        return intersections
    }

    private fun calculateOperations(span: Span<T>, intersections: SpansIntersections<T>): List<SpanOperation> {
        val result = mutableListOf<SpanOperation>()

        // Without intersections
        if(!intersections.hasIntersections()) {
            spans.add(span)
            result.add(CreateSpanOperation(span))
            return result
        }

        // Has full intersection
        val fullIntersection = intersections.spansIntersectFull.firstOrNull()
        if(fullIntersection != null) {
            if(fullIntersection.value == span.value) {
                return result
            }

            val newSpanValue = calculateSpanValue(fullIntersection.value, span.value)
            if(fullIntersection.value == newSpanValue) {
                return result
            }

            spans.remove(fullIntersection)
            result.add(DeleteSpanOperation(fullIntersection.id))

            newSpanValue?.let {
                span.copy(it).let { newSpan ->
                    spans.add(newSpan)
                    result.add(CreateSpanOperation(newSpan))
                }
            }

            return result
        }

        // Has old full-outside span
        val oldFullOutside = intersections.spansOutsideFull.firstOrNull()
        if(oldFullOutside != null) {
            if(oldFullOutside.value == span.value) {
                return result
            }

            val newSpanValue = calculateSpanValue(oldFullOutside.value, span.value)
            if(oldFullOutside.value == newSpanValue) {
                return result
            }

            spans.remove(oldFullOutside)
            result.add(DeleteSpanOperation(oldFullOutside.id))

            val newLeftSpan = create(oldFullOutside.range.first..span.range.first, oldFullOutside.value)
            spans.add(newLeftSpan)
            result.add(CreateSpanOperation(newLeftSpan))

            val newRightSpan = create(span.range.last..oldFullOutside.range.last, oldFullOutside.value)
            spans.add(newRightSpan)
            result.add(CreateSpanOperation(newRightSpan))

            newSpanValue?.let {
                span.copy(it).let { newSpan ->
                    spans.add(newSpan)
                    result.add(CreateSpanOperation(newSpan))
                }
            }

            return result
        }

        // Remove full inside spans
        intersections.spansInsideFull.forEach { insideSpan ->
            spans.remove(insideSpan)
            result.add(CreateSpanOperation(insideSpan))
        }

        // Process left-intersected spans
        intersections.spansInsideLeft.forEach { oldLeftSpan ->
            create(oldLeftSpan.range.first..span.range.first, oldLeftSpan.value).let { newLeftSpan ->
                spans.remove(oldLeftSpan)
                result.add(DeleteSpanOperation(oldLeftSpan.id))

                spans.add(newLeftSpan)
                result.add(CreateSpanOperation(newLeftSpan))

            }
        }

        // Process right-intersected spans
        intersections.spansInsideRight.forEach { oldRightSpan ->
            create(span.range.last..oldRightSpan.range.last, oldRightSpan.value).let { newRightSpan ->
                spans.remove(oldRightSpan)
                result.add(DeleteSpanOperation(oldRightSpan.id))

                spans.add(newRightSpan)
                result.add(CreateSpanOperation(newRightSpan))

            }
        }

        // Add new span
        spans.add(span)
        result.add(CreateSpanOperation(span))

        return result
    }
}