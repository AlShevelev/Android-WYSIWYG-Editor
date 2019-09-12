package com.github.irshulx.components.input.spans

class SpansIntersections<T>() {
    val spansIntersectFull = mutableListOf<Span<T>>()

    val spansInsideFull = mutableListOf<Span<T>>()

    val spansInsideLeft = mutableListOf<Span<T>>()

    val spansInsideRight = mutableListOf<Span<T>>()

    /**
     * The list of spans that cover completely the new one
     */
    val spansOutsideFull = mutableListOf<Span<T>>()

    fun hasIntersections() = spansIntersectFull.isNotEmpty() || spansInsideFull.isNotEmpty() || spansInsideLeft.isNotEmpty() ||
            spansInsideRight.isNotEmpty() || spansOutsideFull.isNotEmpty()
}