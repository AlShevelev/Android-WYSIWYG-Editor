package com.github.irshulx.components.input.spans

abstract class SpansCollection<T> {
    private val spans = mutableListOf<Span<T>>()

    protected fun add(span: Span<T>): List<SpanOperation> {
        if(span.area.first >= span.area.last) {
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

    protected abstract fun create(area: IntRange, newValue: T): Span<T>

    private fun calculateIntersections(span: Span<T>): SpansIntersections<T> {
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
            val newSpanValue = calculateSpanValue(oldFullOutside.value, span.value)
            if(oldFullOutside.value == newSpanValue) {
                return result
            }

            spans.remove(oldFullOutside)
            result.add(DeleteSpanOperation(oldFullOutside.id))

            val newLeftSpan = create(oldFullOutside.area.first..span.area.first, oldFullOutside.value)
            spans.add(newLeftSpan)
            result.add(CreateSpanOperation(newLeftSpan))

            val newRightSpan = create(span.area.last..oldFullOutside.area.last, oldFullOutside.value)
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
            result.add(DeleteSpanOperation(insideSpan.id))
        }

        // Process left-intersected spans
        intersections.spansInsideLeft.forEach { oldLeftSpan ->
            create(oldLeftSpan.area.first..span.area.first, oldLeftSpan.value).let { newLeftSpan ->
                spans.remove(oldLeftSpan)
                result.add(DeleteSpanOperation(oldLeftSpan.id))

                spans.add(newLeftSpan)
                result.add(CreateSpanOperation(newLeftSpan))

            }
        }

        // Process right-intersected spans
        intersections.spansInsideRight.forEach { oldRightSpan ->
            create(span.area.last..oldRightSpan.area.last, oldRightSpan.value).let { newRightSpan ->
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