package com.github.irshulx.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.text.util.Linkify
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.irshulx.EditorComponent
import com.github.irshulx.EditorCore
import com.github.irshulx.R
import com.github.irshulx.components.input.edit_text.CustomEditText
import com.github.irshulx.models.*
import com.github.irshulx.models.control_metadata.ControlMetadata
import com.github.irshulx.models.control_metadata.ImageDescriptionMetadata
import com.github.irshulx.models.control_metadata.ImageMetadata
import com.github.irshulx.utilities.toHtml
import org.jsoup.nodes.Element
import java.util.*

class ImageExtensions(private val editorCore: EditorCore) : EditorComponent(editorCore) {
    private var requestListener: RequestListener<Drawable>? = null
    private var requestOptions: RequestOptions? = null
    private var transition: DrawableTransitionOptions? = null

    @DrawableRes
    var placeholder = -1
    @DrawableRes
    var errorBackground = -1

    override fun getContent(view: View): Node {
        val node = getNodeInstance(view)
        val imgTag = view.tag as ImageMetadata

        if (!imgTag.path.isNullOrEmpty()) {
            node.content!!.add(imgTag.path!!)

            // for subtitle
            val textView = view.findViewById<EditText>(R.id.descriptionText)

            val subTitleNode = getNodeInstance(textView)

            val descTag = textView.tag as ImageDescriptionMetadata
            subTitleNode.editorTextStyles = descTag.editorTextStyles
            subTitleNode.textSettings = descTag.textSettings

            val desc = textView.text
            subTitleNode.content!!.add(desc.toHtml())
            node.childs = ArrayList()
            node.childs!!.add(subTitleNode)
        }
        return node
    }

    override fun getContentAsHTML(node: Node, content: EditorContent): String {
        val subHtml = componentsWrapper!!.inputExtensions!!.getInputHtml()
        var html = componentsWrapper!!.htmlExtensions!!.getTemplateHtml(node.type!!)
        html = html.replace("{{\$url}}", node.content!![0])
        html = html.replace("{{\$img-sub}}", subHtml)
        return html
    }

    override fun renderEditorFromState(node: Node, content: EditorContent) {
        val path = node.content!![0]
        if (editorCore.renderType === RenderType.RENDERER) {
            loadImage(path, node.childs!![0])
        } else {
            val layout = insertImage(null, path, editorCore.childCount, node.childs!![0].content!![0], false)
            componentsWrapper!!.inputExtensions!!.applyTextSettings(node.childs!![0], layout.findViewById<View>(R.id.descriptionText) as TextView)
        }
    }

    override fun buildNodeFromHTML(element: Element): Node? {
        val tag = HtmlTag.valueOf(element.tagName().toLowerCase(Locale.ROOT))
        if (tag === HtmlTag.div) {
            val dataTag = element.attr("data-tag")
            if (dataTag == "img") {
                val img = element.child(0)
                val descTag = element.child(1)
                val src = img.attr("src")
                loadImage(src, descTag)
            }
        } else {
            val src = element.attr("src")
            if (element.children().size > 0) {
                val descTag = element.child(1)
                loadImage(src, descTag)
            } else {
                loadImageRemote(src, null)
            }
        }
        return null
    }

    override fun init(componentsWrapper: ComponentsWrapper) {
        this.componentsWrapper = componentsWrapper
    }

    fun openImageGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        (editorCore.context as Activity).startActivityForResult(Intent.createChooser(intent, "Select an image"), EditorCore.PICK_IMAGE_REQUEST)
    }

    @SuppressLint("InflateParams")
    fun insertImage(image: Bitmap?, url: String?, index: Int, subTitle: String?, appendTextline: Boolean): View {
        var determineIndex = index
        var hasUploaded = false
        if (!TextUtils.isEmpty(url)) hasUploaded = true

        // Render(getStateFromString());
        val childLayout = (editorCore.context as Activity).layoutInflater.inflate(R.layout.widget_image_view, null)
        val imageView = childLayout.findViewById<ImageView>(R.id.imageView)
        val lblStatus = childLayout.findViewById<TextView>(R.id.lblStatus)

        val desc = childLayout.findViewById<CustomEditText>(R.id.descriptionText)

        if (!url.isNullOrEmpty()) {
            loadImageUsingLib(url, imageView)
        } else {
            imageView.setImageBitmap(image)
        }
        val uniqueId = UUID.randomUUID().toString()
        if (determineIndex == -1) {
            determineIndex = editorCore.determineIndex(EditorType.IMG)
        }
        showNextInputHint(determineIndex)
        editorCore.parentView!!.addView(childLayout, determineIndex)

        // set the imageId,so we can recognize later after upload
        childLayout.tag = createImageTag(if (hasUploaded) url else uniqueId)
        desc.tag = createSubTitleTag()

        desc.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                desc.clearFocus()
            } else {
                editorCore.activeView = desc
            }
        }

        if (editorCore.isLastRow(childLayout) && appendTextline) {
            componentsWrapper!!.inputExtensions!!.insertEditText(determineIndex + 1, null)
        }
        if (!subTitle.isNullOrEmpty())
            componentsWrapper!!.inputExtensions!!.setText(desc, subTitle)

        if (editorCore.renderType === RenderType.EDITOR) {
            bindEvents(childLayout)
            if (!hasUploaded) {
                lblStatus.visibility = View.VISIBLE
                childLayout.findViewById<View>(R.id.progress).visibility = View.VISIBLE
                editorCore.editorListener!!.onUpload(image!!, uniqueId)
            }
        } else {
            desc.isEnabled = false
            lblStatus.visibility = View.GONE
        }

        return childLayout
    }

    private fun showNextInputHint(index: Int) {
        val view = editorCore.parentView!!.getChildAt(index)
        val type = editorCore.getControlType(view)
        if (type !== EditorType.INPUT)
            return
        val tv = view as TextView
        tv.hint = editorCore.placeHolder
        Linkify.addLinks(tv, Linkify.ALL)
    }

    private fun hideInputHint(index: Int) {
        val view = editorCore.parentView!!.getChildAt(index)
        val type = editorCore.getControlType(view)
        if (type !== EditorType.INPUT)
            return

        var hint = editorCore.placeHolder
        if (index > 0) {
            val prevView = editorCore.parentView!!.getChildAt(index - 1)
            val prevType = editorCore.getControlType(prevView)
            if (prevType === EditorType.INPUT)
                hint = null
        }
        val tv = view as TextView
        tv.hint = hint
    }

    @SuppressLint("CheckResult")
    fun loadImageUsingLib(path: String, imageView: ImageView) {
        if (requestListener == null) {
            requestListener = object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    return false
                }
            }
        }

        if (placeholder == -1) {
            placeholder = R.drawable.image_placeholder
        }

        if (errorBackground == -1) {
            errorBackground = R.drawable.error_background
        }

        if (requestOptions == null) {
            requestOptions = RequestOptions()
        }

        requestOptions!!.placeholder(placeholder)
        requestOptions!!.error(errorBackground)

        if (transition == null) {
            transition = DrawableTransitionOptions.withCrossFade().crossFade(1000)
        }
        Glide.with(imageView.context)
                .load(path)
                .transition(transition!!)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)   //No disk cache
                .listener(requestListener)
                .apply(requestOptions!!)
                .into(imageView)
    }

    fun onPostUpload(url: String?, imageId: String) {
        val view = findImageById(imageId)
        val lblStatus = view!!.findViewById<View>(R.id.lblStatus) as TextView
        lblStatus.text = if (!TextUtils.isEmpty(url)) "Upload complete" else "Upload failed"
        if (!url.isNullOrEmpty()) {

            val control = ImageMetadata(EditorType.IMG)
            control.path = url
            view.tag = control

            val timerTask = object : TimerTask() {
                override fun run() {
                    (editorCore.context as Activity).runOnUiThread {
                        // This code will always run on th UI thread, therefore is safe to modify UI elements.
                        lblStatus.visibility = View.GONE
                    }
                }
            }
            Timer().schedule(timerTask, 3000)
        }
        view.findViewById<View>(R.id.progress).visibility = View.GONE
    }


    private fun bindEvents(layout: View) {
        val imageView = layout.findViewById<ImageView>(R.id.imageView)
        val btnRemove = layout.findViewById<View>(R.id.btn_remove)

        btnRemove.setOnClickListener {
            val index = editorCore.parentView!!.indexOfChild(layout)
            editorCore.parentView!!.removeView(layout)
            hideInputHint(index)
            componentsWrapper!!.inputExtensions!!.setFocusToPrevious(index)
        }

        layout.setOnTouchListener(View.OnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val paddingTop = view.paddingTop
                val paddingBottom = view.paddingBottom
                val height = view.height

                when {
                    event.y < paddingTop -> editorCore.onViewTouched(0, editorCore.parentView!!.indexOfChild(layout))
                    event.y > height - paddingBottom -> editorCore.onViewTouched(1, editorCore.parentView!!.indexOfChild(layout))
                }

                return@OnTouchListener false
            }
            true        //hmmmm....
        })

        imageView.setOnClickListener { btnRemove.visibility = View.VISIBLE }
        imageView.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            btnRemove.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }
    }

    private fun findImageById(imageId: String): View? {
        for (i in 0 until editorCore.parentChildCount) {
            val view = editorCore.parentView!!.getChildAt(i)

            val control = editorCore.getControlMetadata(view)
            if(control is ImageMetadata) {
                if (!TextUtils.isEmpty(control.path) && control.path == imageId) {
                    return view
                }
            }
        }
        return null
    }

    private fun createSubTitleTag(): ControlMetadata {
        val subTag = ImageDescriptionMetadata(EditorType.IMG_SUB)
        subTag.textSettings = TextSettings("#5E5E5E")
        return subTag
    }

    private fun createImageTag(path: String?): ControlMetadata {
        val control = ImageMetadata(EditorType.IMG)
        control.path = path
        return control
    }

    /**
     * used by the renderer to render the image from the Node
     */
    private fun loadImage(_path: String, node: Node) {
        val desc = node.content!![0]
        val childLayout = loadImageRemote(_path, desc)
        val text = childLayout.findViewById<CustomEditText>(R.id.descriptionText)
        if (!TextUtils.isEmpty(desc)) {
            componentsWrapper!!.inputExtensions!!.applyTextSettings(node, text)
        }
    }

    private fun loadImage(_path: String, node: Element?) {
        var desc: String? = null
        if (node != null) {
            desc = node.html()
        }
        val childLayout = loadImageRemote(_path, desc)
        val text = childLayout.findViewById<CustomEditText>(R.id.descriptionText)
        if (node != null) {
            componentsWrapper!!.inputExtensions!!.applyStyles(text, node)
        }
    }

    @SuppressLint("InflateParams")
    private fun loadImageRemote(path: String, desc: String?): View {
        val childLayout = (editorCore.context as Activity).layoutInflater.inflate(R.layout.widget_image_view, null)
        val imageView = childLayout.findViewById<ImageView>(R.id.imageView)
        val text = childLayout.findViewById<CustomEditText>(R.id.descriptionText)

        childLayout.tag = createImageTag(path)
        text.tag = createSubTitleTag()
        if (!desc.isNullOrEmpty()) {
            componentsWrapper!!.inputExtensions!!.setText(text, desc)
        }
        text.isEnabled = editorCore.renderType === RenderType.EDITOR
        loadImageUsingLib(path, imageView)
        editorCore.parentView!!.addView(childLayout)

        if (editorCore.renderType === RenderType.EDITOR) {
            bindEvents(childLayout)
        }

        return childLayout
    }
}