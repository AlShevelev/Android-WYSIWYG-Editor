package com.github.irshulx.components.input.spans.custom

import androidx.annotation.ColorInt

class LinkSpan(
    value: String,
    @ColorInt textColor: Int
) : SpecialSpansBase<String>(value, textColor) {

    private val typeId = 2525358

    override fun getSpanTypeId(): Int = typeId
}