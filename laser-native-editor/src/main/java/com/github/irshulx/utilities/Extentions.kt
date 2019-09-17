package com.github.irshulx.utilities

import android.os.Build
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.text.Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
import android.text.Spannable
import android.text.Spanned

fun Spannable.toHtml(): String =
    if(Build.VERSION.SDK_INT < 24) {
        @Suppress("DEPRECATION")
        Html.toHtml(this)
    } else {
        Html.toHtml(this, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
    }

fun String.fromHtml(): Spanned =
    if(Build.VERSION.SDK_INT < 24) {
        @Suppress("DEPRECATION")
        Html.fromHtml(this)
    } else {
        Html.fromHtml(this, FROM_HTML_MODE_LEGACY, null, null)
    }
