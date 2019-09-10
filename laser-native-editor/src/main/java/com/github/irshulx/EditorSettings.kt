package com.github.irshulx

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.widget.LinearLayout

import com.github.irshulx.models.RenderType
import com.google.gson.Gson

class EditorSettings(internal var context: Context) {
    internal var placeHolder: String? = null
    internal var autoFocus = true
    internal var serialRenderInProgress = false
    internal var parentView: LinearLayout? = null
    internal var renderType: RenderType? = null
    internal var resources: Resources
    internal var activeView: View? = null
    internal var gson: Gson
    internal var stateFresh: Boolean = false

    init {
        this.stateFresh = true
        this.resources = context.resources
        gson = Gson()
        this.parentView = null
    }

    companion object {

        fun init(context: Context, editorCore: EditorCore): EditorSettings {
            val editorSettings = EditorSettings(context)
            editorSettings.parentView = editorCore
            return editorSettings
        }
    }
}