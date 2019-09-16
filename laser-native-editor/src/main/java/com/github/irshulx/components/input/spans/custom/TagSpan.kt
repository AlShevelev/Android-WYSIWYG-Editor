package com.github.irshulx.components.input.spans.custom

import androidx.annotation.ColorInt

class TagSpan(
    value: String,
    @ColorInt textColor: Int
) : SpecialSpansBase<String>(value, textColor) {

    private val typeId = 365498

    override fun getSpanTypeId(): Int = typeId
}