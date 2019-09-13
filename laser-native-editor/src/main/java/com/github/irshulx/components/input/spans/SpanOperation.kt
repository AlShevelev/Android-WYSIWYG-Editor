package com.github.irshulx.components.input.spans

import android.text.style.CharacterStyle

sealed class SpanOperation()

data class DeleteSpanOperation(val span: CharacterStyle): SpanOperation()

data class CreateSpanOperation<T>(val spanInfo: SpanInfo<T>): SpanOperation()