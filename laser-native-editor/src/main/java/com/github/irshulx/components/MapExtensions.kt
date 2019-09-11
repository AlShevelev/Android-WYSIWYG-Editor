package com.github.irshulx.components

import android.app.Activity
import android.view.View
import android.widget.ImageView

import com.github.irshulx.EditorComponent
import com.github.irshulx.EditorCore
import com.github.irshulx.R
import com.github.irshulx.Utilities.Utilities
import com.github.irshulx.components.edit_text.CustomEditText
import com.github.irshulx.models.EditorContent
import com.github.irshulx.models.EditorControl
import com.github.irshulx.models.EditorType
import com.github.irshulx.models.Node
import com.github.irshulx.models.RenderType
import org.jsoup.nodes.Element

/**
 * Created by mkallingal on 5/1/2016.
 */
class MapExtensions(internal var editorCore: EditorCore) : EditorComponent(editorCore) {
    private var mapExtensionTemplate = R.layout.image_view

    override fun getContent(view: View): Node {
        val node = getNodeInstance(view)
        val mapTag = view.tag as EditorControl
        val desc = (view.findViewById<View>(R.id.descriptionText) as CustomEditText).text

        mapTag.cords?.let { node.content!!.add(it) }
        node.content!!.add(if (!desc.isNullOrEmpty()) desc.toString() else "")

        return node
    }

    override fun getContentAsHTML(node: Node, content: EditorContent): String {
        return componentsWrapper!!.htmlExtensions!!.getTemplateHtml(node.type!!).replace("{{\$content}}",
                componentsWrapper!!.mapExtensions!!.getCordsAsUri(node.content!![0])).replace("{{\$desc}}", node.content!![1])
    }

    override fun renderEditorFromState(node: Node, content: EditorContent) {
        insertMap(node.content!![0], node.content!![1], true)
    }

    override fun buildNodeFromHTML(element: Element): Node? {
        return null
    }

    override fun init(componentsWrapper: ComponentsWrapper) {
        this.componentsWrapper = componentsWrapper
    }

    fun setMapViewTemplate(drawable: Int) {
        this.mapExtensionTemplate = drawable
    }


    fun getMapStaticImgUri(cords: String, width: Int): String {
        val builder = StringBuilder()
        builder.append("http://maps.google.com/maps/api/staticmap?")
        builder.append("size=" + width.toString() + "x400&zoom=15&sensor=true&markers=" + cords)
        return builder.toString()
    }

    fun insertMap(cords: String, desc: String, insertEditText: Boolean) {
        //        String image="http://maps.googleapis.com/maps/api/staticmap?center=43.137022,13.067162&zoom=16&size=600x400&maptype=roadmap&sensor=true&markers=color:blue|43.137022,13.067162";
        val x = cords.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val lat = x[0]
        val lng = x[1]
        val size = Utilities.getScreenDimension(editorCore.context)
        val width = size.width

        val childLayout = (this.editorCore.context as Activity).layoutInflater.inflate(this.mapExtensionTemplate, null)
        val imageView = childLayout.findViewById<ImageView>(R.id.imageView)
        componentsWrapper!!.imageExtensions!!.loadImageUsingLib(getMapStaticImgUri("$lat,$lng", width), imageView)

        /**
         * description, if render mode, set the description and disable it
         */
        val editText = childLayout.findViewById<CustomEditText>(R.id.descriptionText)
        if (editorCore.renderType === RenderType.RENDERER) {
            editText.setText(desc)
            editText.isEnabled = false
        }
        /*
         *  remove button
         */

        val btn = childLayout.findViewById<View>(R.id.btn_remove)
        imageView.setOnClickListener { btn.visibility = View.VISIBLE }
        imageView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            btn.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
        }

        btn.setOnClickListener { editorCore.parentView!!.removeView(childLayout) }
        val control = editorCore.createTag(EditorType.MAP)
        control.cords = cords
        childLayout.tag = control
        val Index = editorCore.determineIndex(EditorType.MAP)
        editorCore.parentView!!.addView(childLayout, Index)
        if (insertEditText) {
            componentsWrapper!!.inputExtensions!!.insertEditText(Index + 1, null, null!!)
        }
    }

    fun getCordsAsUri(s: String): String {
        return getMapStaticImgUri(s, 800)
    }
}
