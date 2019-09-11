package com.github.irshulx.components

import android.app.Activity
import android.graphics.Color
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import com.github.irshulx.EditorCore
import com.github.irshulx.EditorComponent
import com.github.irshulx.R
import com.github.irshulx.Utilities.Utilities
import com.github.irshulx.models.EditorContent
import com.github.irshulx.models.EditorControl
import com.github.irshulx.models.EditorType
import com.github.irshulx.models.Node
import com.github.irshulx.models.RenderType

import org.jsoup.nodes.Element

import java.util.HashMap

class MacroExtensions(private val editorCore: EditorCore) : EditorComponent(editorCore) {

    fun insertMacro(name: String, view: View, settings: MutableMap<String, Any>?, index: Int) {
        var index = index
        val frameLayout = FrameLayout(editorCore.context)
        frameLayout.addView(view)
        val overlay = FrameLayout(frameLayout.context)
        overlay.visibility = View.GONE
        overlay.setPadding(0, 0, 20, 0)
        overlay.setBackgroundColor(Color.argb(50, 0, 0, 0))
        val imageView = ImageView(overlay.context)
        val params = FrameLayout.LayoutParams(Utilities.dpToPx(frameLayout.context, 40f), Utilities.dpToPx(frameLayout.context, 40f))
        params.gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
        imageView.layoutParams = params
        imageView.setImageResource(R.drawable.ic_close_white_36dp)
        overlay.addView(imageView)
        frameLayout.addView(overlay)
        imageView.setOnClickListener { editorCore.parentView!!.removeView(frameLayout) }

        val control = editorCore.createTag(EditorType.MACRO)
        control.macroSettings = settings
        control.macroName = name
        if (index == -1) {
            index = editorCore.determineIndex(EditorType.MACRO)
        }
        frameLayout.tag = control

        editorCore.parentView!!.addView(frameLayout, index)

        if (editorCore.renderType === RenderType.RENDERER) return

        view.setOnTouchListener(View.OnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val paddingTop = view.paddingTop
                val paddingBottom = view.paddingBottom
                val height = view.height
                if (event.y < paddingTop) {
                    editorCore.___onViewTouched(0, editorCore.parentView!!.indexOfChild(frameLayout))
                } else if (event.y > height - paddingBottom) {
                    editorCore.___onViewTouched(1, editorCore.parentView!!.indexOfChild(frameLayout))
                } else {
                    if (overlay.visibility == View.VISIBLE) {
                        overlay.visibility = View.GONE
                    } else {
                        overlay.visibility = View.VISIBLE
                    }
                }
                return@OnTouchListener false
            }
            true//hmmmm....
        })

        frameLayout.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            if (!b) {
                overlay.visibility = View.GONE
            }
        }
    }

    override fun getContent(view: View): Node {
        val node = this.getNodeInstance(view)
        val macroTag = view.tag as EditorControl

        macroTag.macroName?.let {node.content!!.add(it)}

        node.macroSettings = macroTag.macroSettings
        return node
    }

    override fun getContentAsHTML(node: Node, content: EditorContent): String {
        return getAsHtml(node.content!![0], node.macroSettings!!)
    }

    private fun getAsHtml(name: String, macroSettings: Map<String, Any>): String {
        var template = "<{{\$tag}} data-tag=\"macro\" {{\$settings}}></{{\$tag}}>"
        template = template.replace("{{\$tag}}", name)
        val dataTags = StringBuilder()
        for ((key, value) in macroSettings) {

            if (key.equals("data-tag", ignoreCase = true)) continue


            dataTags.append(" ")
            if (key.contains("data-")) {
                dataTags.append(key)
            } else {
                dataTags.append("data-$key")
            }
            dataTags.append("=\"").append(value.toString()).append("\"")
        }
        if (TextUtils.isEmpty(dataTags)) {
            template = template.replace("{{\$settings}}", "")
        } else {
            template = template.replace("{{\$settings}}", dataTags.toString())
        }
        return template
    }

    override fun renderEditorFromState(node: Node, content: EditorContent) {
        val index = editorCore.childCount
        var view: View? = editorCore.editorListener!!.onRenderMacro(node.content!![0], node.macroSettings!!, editorCore.childCount)

        if (view == null)
            view = getEmptyMacro(node.content!![0], node.macroSettings)

        insertMacro(node.content!![0], view, node.macroSettings, index)
    }

    private fun getEmptyMacro(name: String, macroSettings: Map<String, Any>?): View {
        val layout = (editorCore.context as Activity).layoutInflater.inflate(R.layout.default_macro, null)
        val message = layout.findViewById<TextView>(R.id.txtMessage)
        message.text = "Unhandled macro " + "\"" + getAsHtml(name, macroSettings!!) + "\""
        return layout
    }

    override fun buildNodeFromHTML(element: Element): Node? {
        val tag = element.tagName().toLowerCase()
        val node = getNodeInstance(EditorType.MACRO)
        node.content!!.add(tag)

        val attrs = element.attributes().asList()

        if (!attrs.isEmpty()) {
            node.macroSettings = HashMap()
            for ((key, value) in attrs) {
                node.macroSettings!![key] = value
            }
        }
        val index = editorCore.childCount
        var view: View? = editorCore.editorListener!!.onRenderMacro(tag, node.macroSettings!!, editorCore.childCount)

        if (view == null)
            view = getEmptyMacro(node.content!![0], node.macroSettings)

        insertMacro(tag, view, node.macroSettings, index)
        return null
    }

    override fun init(componentsWrapper: ComponentsWrapper) {
        this.componentsWrapper = componentsWrapper
    }

}
