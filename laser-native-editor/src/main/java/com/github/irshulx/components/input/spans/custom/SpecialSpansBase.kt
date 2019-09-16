package com.github.irshulx.components.input.spans.custom

import android.text.TextPaint
import android.text.style.UnderlineSpan
import androidx.annotation.ColorInt

abstract class SpecialSpansBase<T>(
    val value: T,
    @ColorInt private val textColor: Int
) : UnderlineSpan() {

    abstract override fun getSpanTypeId(): Int

    override fun describeContents(): Int = 0

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.color = textColor
    }
}