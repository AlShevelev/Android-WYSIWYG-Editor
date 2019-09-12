package com.github.irshulx.components.input.spans

sealed class SpanOperation()

data class DeleteSpanOperation(val spanId: Long): SpanOperation()

data class CreateSpanOperation<T>(val span: Span<T>): SpanOperation()