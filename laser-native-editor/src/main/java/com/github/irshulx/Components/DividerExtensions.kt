package com.github.irshulx.Components

import android.app.Activity
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

import com.github.irshulx.EditorComponent
import com.github.irshulx.R
import com.github.irshulx.EditorCore
import com.github.irshulx.models.EditorContent
import com.github.irshulx.models.EditorType
import com.github.irshulx.models.Node
import com.github.irshulx.models.RenderType

import org.jsoup.nodes.Element

class DividerExtensions(internal var editorCore: EditorCore) : EditorComponent(editorCore) {
    private var dividerLayout = R.layout.tmpl_divider_layout

    override fun getContent(view: View): Node {
        return getNodeInstance(view)
    }

    override fun getContentAsHTML(node: Node, content: EditorContent): String {
        return componentsWrapper!!.htmlExtensions!!.getTemplateHtml(EditorType.HR)
    }

    override fun renderEditorFromState(node: Node, content: EditorContent) {
        insertDivider(content.nodes!!.indexOf(node))
    }

    override fun buildNodeFromHTML(element: Element): Node? {
        val count = editorCore.childCount
        insertDivider(count)
        return null
    }

    override fun init(componentsWrapper: ComponentsWrapper) {
        this.componentsWrapper = componentsWrapper
    }

    fun setDividerLayout(layout: Int) {
        this.dividerLayout = layout
    }

    fun insertDivider(index: Int) {
        var index = index
        val view = (editorCore.context as Activity).layoutInflater.inflate(this.dividerLayout, null)
        view.tag = editorCore.createTag(EditorType.HR)
        if (index == -1) {
            index = editorCore.determineIndex(EditorType.HR)
        }
        if (index == 0) {
            Toast.makeText(editorCore.context, "divider cannot be inserted on line zero", Toast.LENGTH_SHORT).show()
            return
        }
        editorCore.parentView!!.addView(view, index)

        if (editorCore.renderType === RenderType.EDITOR) {

            if (editorCore.getControlType(editorCore.parentView!!.getChildAt(index + 1)) === EditorType.INPUT) {
                val customEditText = editorCore.getChildAt(index + 1) as CustomEditText
                componentsWrapper!!.inputExtensions!!.removeFocus(customEditText)
            }
            view.setOnTouchListener(View.OnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val paddingTop = view.paddingTop
                    val paddingBottom = view.paddingBottom
                    val height = view.height
                    if (event.y < paddingTop) {
                        editorCore.___onViewTouched(0, editorCore.parentView!!.indexOfChild(view))
                    } else if (event.y > height - paddingBottom) {
                        editorCore.___onViewTouched(1, editorCore.parentView!!.indexOfChild(view))
                    }
                    return@OnTouchListener false
                }
                true
            })

            val focus = editorCore.activity.currentFocus
            if (focus != null) {
                val imm = editorCore.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)

                if (focus is CustomEditText) {
                    focus.clearFocus()
                    editorCore.parentView!!.requestFocus()
                }
            }

        }
    }

    fun deleteHr(indexOfDeleteItem: Int): Boolean {
        val view = editorCore.parentView!!.getChildAt(indexOfDeleteItem)
        if (view == null || editorCore.getControlType(view) === EditorType.HR) {
            editorCore.parentView!!.removeView(view)
            return true
        }
        return false
    }

    fun removeAllDividersBetweenDeletedAndFocusNext(indexOfDeleteItem: Int, nextFocusIndex: Int) {
        for (i in nextFocusIndex until indexOfDeleteItem) {
            if (editorCore.getControlType(editorCore.parentView!!.getChildAt(i)) === EditorType.HR) {
                editorCore.parentView!!.removeViewAt(i)
            }
        }
    }
}