package com.github.irshulx.Utilities

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout

import com.github.irshulx.models.HtmlTag

class HtmlParser(private val context: Context) {
    internal var parentView: LinearLayout = LinearLayout(this.context)
            .apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

    companion object {
        fun matchesTag(test: String): Boolean {
            for (tag in HtmlTag.values()) {
                if (tag.name == test) {
                    return true
                }
            }
            return false
        }
    }
}