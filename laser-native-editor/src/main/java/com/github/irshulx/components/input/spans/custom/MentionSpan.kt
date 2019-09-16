package com.github.irshulx.components.input.spans.custom

import androidx.annotation.ColorInt

class MentionSpan(
    value: String,
    @ColorInt textColor: Int
) : SpecialSpansBase<String>(value, textColor) {

    private val typeId = 6739286

    override fun getSpanTypeId(): Int = typeId
}