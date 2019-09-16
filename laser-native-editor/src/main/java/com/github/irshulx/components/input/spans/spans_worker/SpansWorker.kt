package com.github.irshulx.components.input.spans.spans_worker

import android.text.style.CharacterStyle

interface SpansWorker: SpansWorkerRead {
    fun removeSpan(span: CharacterStyle)

    fun createSpan(span: CharacterStyle, interval: IntRange)
}