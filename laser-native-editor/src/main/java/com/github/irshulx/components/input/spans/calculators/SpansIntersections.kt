package com.github.irshulx.components.input.spans.calculators

class SpansIntersections<T> {
    val spansIntersectFull = mutableListOf<SpanInfo<T>>()

    val spansInsideFull = mutableListOf<SpanInfo<T>>()

    val spansInsideLeft = mutableListOf<SpanInfo<T>>()

    val spansInsideRight = mutableListOf<SpanInfo<T>>()

    /**
     * The list of spans that cover completely the new one
     */
    val spansOutsideFull = mutableListOf<SpanInfo<T>>()

    fun hasIntersections() = spansIntersectFull.isNotEmpty() || spansInsideFull.isNotEmpty() || spansInsideLeft.isNotEmpty() ||
            spansInsideRight.isNotEmpty() || spansOutsideFull.isNotEmpty()
}