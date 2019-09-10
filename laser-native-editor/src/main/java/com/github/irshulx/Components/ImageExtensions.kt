package com.github.irshulx.Components

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.text.Html
import android.text.TextUtils
import android.text.util.Linkify
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
import com.github.irshulx.models.*
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.*

class ImageExtensions(private val editorCore: EditorCore) : EditorComponent(editorCore) {
    private var editorImageLayout = R.layout.tmpl_image_view
    var requestListener: RequestListener<Drawable>? = null
    var requestOptions: RequestOptions? = null
    var transition: DrawableTransitionOptions? = null

    @DrawableRes
    var placeholder = -1
    @DrawableRes
    var errorBackground = -1

    override fun getContent(view: View): Node {
        val node = getNodeInstance(view)
        val imgTag = view.tag as EditorControl
        if (!imgTag.path.isNullOrEmpty()) {
            node.content!!.add(imgTag.path!!)

            /**
             * for subtitle
             */
            val textView = view.findViewById<EditText>(R.id.desc)

            val subTitleNode = getNodeInstance(textView)
            val descTag = textView.tag as EditorControl
            subTitleNode.contentStyles = descTag.editorTextStyles
            subTitleNode.textSettings = descTag.textSettings
            val desc = textView.text
            subTitleNode.content!!.add(Html.toHtml(desc))
            node.childs = ArrayList()
            node.childs!!.add(subTitleNode)
        }
        return node
    }

    override fun getContentAsHTML(node: Node, content: EditorContent): String {
        val subHtml = componentsWrapper!!.inputExtensions!!.getInputHtml(node.childs!![0])
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
            componentsWrapper!!.inputExtensions!!.applyTextSettings(node.childs!![0], layout.findViewById<View>(R.id.desc) as TextView)
        }
    }

    override fun buildNodeFromHTML(element: Element): Node? {
        val tag = HtmlTag.valueOf(element.tagName().toLowerCase())
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

    fun setEditorImageLayout(drawable: Int) {
        this.editorImageLayout = drawable
    }

    fun openImageGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        (editorCore.context as Activity).startActivityForResult(Intent.createChooser(intent, "Select an image"), editorCore.PICK_IMAGE_REQUEST)
    }

    fun insertImage(image: Bitmap?, url: String?, index: Int, subTitle: String?, appendTextline: Boolean): View {
        var index = index
        var hasUploaded = false
        if (!TextUtils.isEmpty(url)) hasUploaded = true

        // Render(getStateFromString());
        val childLayout = (editorCore.context as Activity).layoutInflater.inflate(this.editorImageLayout, null)
        val imageView = childLayout.findViewById<ImageView>(R.id.imageView)
        val lblStatus = childLayout.findViewById<TextView>(R.id.lblStatus)

        val desc = childLayout.findViewById<CustomEditText>(R.id.desc)

        if (!url.isNullOrEmpty()) {
            loadImageUsingLib(url, imageView)
        } else {
            imageView.setImageBitmap(image)
        }
        val uuid = generateUUID()
        if (index == -1) {
            index = editorCore.determineIndex(EditorType.IMG)
        }
        showNextInputHint(index)
        editorCore.parentView!!.addView(childLayout, index)

        //      _Views.add(childLayout);

        // set the imageId,so we can recognize later after upload
        childLayout.tag = createImageTag(if (hasUploaded) url else uuid)
        desc.tag = createSubTitleTag()

        desc.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                desc.clearFocus()
            } else {
                editorCore.activeView = desc
            }
        }

        if (editorCore.isLastRow(childLayout) && appendTextline) {
            componentsWrapper!!.inputExtensions!!.insertEditText(index + 1, null, null)
        }
        if (!subTitle.isNullOrEmpty())
            componentsWrapper!!.inputExtensions!!.setText(desc, subTitle)

        if (editorCore.renderType === RenderType.EDITOR) {
            BindEvents(childLayout)
            if (!hasUploaded) {
                lblStatus.visibility = View.VISIBLE
                childLayout.findViewById<View>(R.id.progress).visibility = View.VISIBLE
                editorCore.editorListener!!.onUpload(image!!, uuid)
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

    fun generateUUID(): String {
        val df = SimpleDateFormat("yyyyMMddHHmmss")
        val sdt = df.format(Date(System.currentTimeMillis()))
        val x = UUID.randomUUID()
        val y = x.toString().split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return y[y.size - 1] + sdt
    }

    fun createSubTitleTag(): EditorControl {
        val subTag = editorCore.createTag(EditorType.IMG_SUB)
        subTag.textSettings = TextSettings("#5E5E5E")
        return subTag
    }

    fun createImageTag(path: String?): EditorControl {
        val control = editorCore.createTag(EditorType.IMG)
        control.path = path
        return control
    }

    /*
      /used by the renderer to render the image from the Node
    */
    fun loadImage(_path: String, node: Node) {
        val desc = node.content!![0]
        val childLayout = loadImageRemote(_path, desc)
        val text = childLayout.findViewById<CustomEditText>(R.id.desc)
        if (!TextUtils.isEmpty(desc)) {
            componentsWrapper!!.inputExtensions!!.applyTextSettings(node, text)
        }
    }

    fun loadImage(_path: String, node: Element?) {
        var desc: String? = null
        if (node != null) {
            desc = node.html()
        }
        val childLayout = loadImageRemote(_path, desc)
        val text = childLayout.findViewById<CustomEditText>(R.id.desc)
        if (node != null) {
            componentsWrapper!!.inputExtensions!!.applyStyles(text, node)
        }
    }

    fun loadImageRemote(path: String, desc: String?): View {
        val childLayout = (editorCore.context as Activity).layoutInflater.inflate(this.editorImageLayout, null)
        val imageView = childLayout.findViewById<ImageView>(R.id.imageView)
        val text = childLayout.findViewById<CustomEditText>(R.id.desc)

        childLayout.tag = createImageTag(path)
        text.tag = createSubTitleTag()
        if (!desc.isNullOrEmpty()) {
            componentsWrapper!!.inputExtensions!!.setText(text, desc)
        }
        text.isEnabled = editorCore.renderType === RenderType.EDITOR
        loadImageUsingLib(path, imageView)
        editorCore.parentView!!.addView(childLayout)

        if (editorCore.renderType === RenderType.EDITOR) {
            BindEvents(childLayout)
        }

        return childLayout
    }


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


    fun findImageById(imageId: String): View? {
        for (i in 0 until editorCore.parentChildCount) {
            val view = editorCore.parentView!!.getChildAt(i)
            val control = editorCore.getControlTag(view)
            if (!TextUtils.isEmpty(control!!.path) && control.path == imageId)
                return view
        }
        return null
    }

    fun onPostUpload(url: String?, imageId: String) {
        val view = findImageById(imageId)
        val lblStatus = view!!.findViewById<View>(R.id.lblStatus) as TextView
        lblStatus.text = if (!TextUtils.isEmpty(url)) "Upload complete" else "Upload failed"
        if (!url.isNullOrEmpty()) {
            val control = editorCore.createTag(EditorType.IMG)
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
            java.util.Timer().schedule(timerTask, 3000)
        }
        view.findViewById<View>(R.id.progress).visibility = View.GONE
    }


    private fun BindEvents(layout: View) {
        val imageView = layout.findViewById<ImageView>(R.id.imageView)
        val btn_remove = layout.findViewById<View>(R.id.btn_remove)

        btn_remove.setOnClickListener {
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
                if (event.y < paddingTop) {
                    editorCore.___onViewTouched(0, editorCore.parentView!!.indexOfChild(layout))
                } else if (event.y > height - paddingBottom) {
                    editorCore.___onViewTouched(1, editorCore.parentView!!.indexOfChild(layout))
                } else {

                }
                return@OnTouchListener false
            }
            true//hmmmm....
        })

        imageView.setOnClickListener { btn_remove.visibility = View.VISIBLE }
        imageView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            btn_remove.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }
    }
}