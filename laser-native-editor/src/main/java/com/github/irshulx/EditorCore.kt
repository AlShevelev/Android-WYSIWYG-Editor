package com.github.irshulx

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Rect
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import com.github.irshulx.components.*
import com.github.irshulx.utilities.Utilities
import com.github.irshulx.components.input.edit_text.CustomEditText
import com.github.irshulx.components.input.InputExtensions
import com.github.irshulx.models.*
import com.github.irshulx.models.control_metadata.ControlMetadata
import com.github.irshulx.models.Node
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.*

open class EditorCore(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), View.OnTouchListener {
    companion object {
        const val TAG = "EDITOR"
        const val PICK_IMAGE_REQUEST = 1
    }

    open var editorListener: EditorListener? = null

    /*
     * Getters and setters for  extensions
     */
    protected var inputExtensions: InputExtensions? = null
        private set

    protected var imageExtensions: ImageExtensions? = null
        private set

    protected var dividerExtensions: DividerExtensions? = null
        private set

    private var htmlExtensions: HTMLExtensions? = null
        private set

    private val editorSettings: EditorSettings = EditorSettings.init(context, this)

    private var componentsWrapper: ComponentsWrapper? = null

    /**
     * returns activity
     * @return
     */
    val activity: Activity
        get() = this.editorSettings.context as Activity

    /**
     * used to get the editor node
     * @return
     */
    val parentView: LinearLayout?
        get() = this.editorSettings.parentView

    /**
     * Get number of childs in the editor
     * @return
     */
    val parentChildCount: Int
        get() = this.editorSettings.parentView!!.childCount

    /**
     * returns whether editor is set as Editor or Rendeder
     * @return
     */
    val renderType: RenderType?
        get() = this.editorSettings.renderType

    /**
     * The current active view on the editor
     * @return
     */
    var activeView: View?
        get() = this.editorSettings.activeView
        set(view) {
            this.editorSettings.activeView = view
        }

    /**
     * Used by Editor
     */
    protected open val contentAsSerialized: String
        get() {
            val state = content
            return serializeContent(state)
        }


    protected open val content: EditorContent?
        get() {
            if (this.editorSettings.renderType === RenderType.RENDERER) {
                Utilities.toastItOut(this.context, "This option only available in editor mode")
                return null
            }

            val childCount = this.editorSettings.parentView!!.childCount
            val editorState = EditorContent()
            val list = ArrayList<Node>()
            for (i in 0 until childCount) {
                val view = this.editorSettings.parentView!!.getChildAt(i)
                var node = getNodeInstance(view)
                when (node.type) {
                    EditorType.INPUT -> {
                        node = inputExtensions!!.getContent(view)
                        list.add(node)
                    }
                    EditorType.IMG -> {
                        node = imageExtensions!!.getContent(view)
                        list.add(node)
                    }
                    EditorType.HR -> {
                        node = dividerExtensions!!.getContent(view)
                        list.add(node)
                    }
                    else -> {}
                }
            }
            editorState.nodes = list
            return editorState
        }

    protected val htmlContent: String
        get() {
            val content = content
            return getHTMLContent(content!!)
        }

    var isStateFresh: Boolean
        get() = this.editorSettings.stateFresh
        set(stateFresh) {
            this.editorSettings.stateFresh = stateFresh
        }


    val placeHolder: String?
        get() = editorSettings.placeHolder

    val autoFocus: Boolean
        get() = editorSettings.autoFocus

    init {
        this.orientation = VERTICAL
        initialize(attrs)
        onPostInit()
    }

    private fun onPostInit() {
        if (renderType === RenderType.EDITOR) {
            setOnTouchListener(this)
        }
    }

    private fun initialize(attrs: AttributeSet) {
        loadStateFromAttrs(attrs)
        inputExtensions = InputExtensions(this)
        imageExtensions = ImageExtensions(this)
        dividerExtensions = DividerExtensions(this)
        htmlExtensions = HTMLExtensions()

        componentsWrapper = ComponentsWrapper.Builder()
            .inputExtensions(inputExtensions!!)
            .htmlExtensions(htmlExtensions!!)
            .dividerExtensions(dividerExtensions!!)
            .imageExtensions(imageExtensions!!)
            .build()

        dividerExtensions!!.init(componentsWrapper!!)
        inputExtensions!!.init(componentsWrapper!!)
        imageExtensions!!.init(componentsWrapper!!)
    }


    fun onViewTouched(hotspot: Int, viewPosition: Int) {
        if (hotspot == 0) {
            if (!inputExtensions!!.isInputTextAtPosition(viewPosition - 1)) {
                inputExtensions!!.insertEditText(viewPosition, null)
            } else {
                Log.d(TAG, "not adding another edittext since already an edittext on the top")
            }
        } else if (hotspot == 1) {
            if (!inputExtensions!!.isInputTextAtPosition(viewPosition + 1)) {
                inputExtensions!!.insertEditText(viewPosition + 1, null)
            } else {
                Log.d(TAG, "not adding another edittext since already an edittext below")
            }
        }
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        onViewTouched(motionEvent)
        return false
    }

    private fun isViewInBounds(view: View, x: Float, y: Float): Boolean {

        val outRect = Rect(view.left, view.top, view.right, view.bottom)

        return outRect.contains(x.toInt(), y.toInt())
    }

    /**
     * no idea what this is
     *
     * @return
     */
    override fun getResources(): Resources {
        return this.editorSettings.resources
    }

    protected open fun getContentAsSerialized(state: EditorContent): String {
        return serializeContent(state)
    }

    protected open fun getContentDeserialized(EditorContentSerialized: String): EditorContent {
        return editorSettings.gson.fromJson(EditorContentSerialized, EditorContent::class.java)
    }

    protected fun renderEditor(_state: EditorContent) {
        this.editorSettings.parentView!!.removeAllViews()
        this.editorSettings.serialRenderInProgress = true
        for (item in _state.nodes!!) {
            when (item.type) {
                EditorType.INPUT -> inputExtensions!!.renderEditorFromState(item, _state)
                EditorType.HR -> dividerExtensions!!.renderEditorFromState(item, _state)
                EditorType.IMG -> imageExtensions!!.renderEditorFromState(item, _state)
                else -> {}
            }
        }
        this.editorSettings.serialRenderInProgress = false
    }

    private fun buildNodeFromHTML(element: Element) {
        val tag = HtmlTag.valueOf(element.tagName().toLowerCase(Locale.ROOT))
        val count = parentView!!.childCount

        if ("br" == tag.name || "<br>" == element.html().replace("\\s+".toRegex(), "") || "<br/>" == element.html().replace("\\s+".toRegex(), "")) {
            inputExtensions!!.insertEditText(count, null)
            return
        } else if ("hr" == tag.name || "<hr>" == element.html().replace("\\s+".toRegex(), "") || "<hr/>" == element.html().replace("\\s+".toRegex(), "")) {
            dividerExtensions!!.buildNodeFromHTML(element)
            return
        }

        when (tag) {
            HtmlTag.img -> imageExtensions!!.buildNodeFromHTML(element)
            HtmlTag.div -> {
                val dataTag = element.attr("data-tag")
                if (dataTag == "img") {
                    imageExtensions!!.buildNodeFromHTML(element)
                } else {
                    inputExtensions!!.buildNodeFromHTML(element)
                }
            }
            else -> {}
        }
    }

    protected fun getHTMLContent(content: EditorContent): String {
        val htmlBlock = StringBuilder()
        var html: String
        for (item in content.nodes!!) {
            when (item.type) {
                EditorType.INPUT -> {
                    html = inputExtensions!!.getContentAsHTML(item, content)
                    htmlBlock.append(html)
                }
                EditorType.IMG -> {
                    val imgHtml = imageExtensions!!.getContentAsHTML(item, content)
                    htmlBlock.append(imgHtml)
                }
                EditorType.HR -> htmlBlock.append(dividerExtensions!!.getContentAsHTML(item, content))
                else -> {}
            }
        }
        return htmlBlock.toString()
    }

    protected fun getHTMLContent(editorContentAsSerialized: String): String {
        val content = getContentDeserialized(editorContentAsSerialized)
        return getHTMLContent(content)
    }


    protected fun renderEditorFromHtml(content: String) {
        this.editorSettings.serialRenderInProgress = true
        parseHtml(content)
        this.editorSettings.serialRenderInProgress = false
    }

    protected open fun clearAllContents() {
        this.editorSettings.parentView!!.removeAllViews()

    }


    @SuppressLint("CustomViewStyleable")
    private fun loadStateFromAttrs(attributeSet: AttributeSet?) {
        if (attributeSet == null) {
            return  // quick exit
        }

        var a: TypedArray? = null
        try {
            a = context.obtainStyledAttributes(attributeSet, R.styleable.editor)
            this.editorSettings.placeHolder = a!!.getString(R.styleable.editor_placeholder)
            this.editorSettings.autoFocus = a.getBoolean(R.styleable.editor_auto_focus, true)
            val renderType = a.getString(R.styleable.editor_render_type)
            if (TextUtils.isEmpty(renderType)) {
                this.editorSettings.renderType = RenderType.EDITOR
            } else {
                this.editorSettings.renderType =
                    if (renderType!!.toLowerCase(Locale.ROOT) == "renderer") {
                        RenderType.RENDERER
                    }
                    else {
                        RenderType.EDITOR
                    }
            }

        } finally {
            a?.recycle()
        }
    }

    /**
     * determine target index for the next insert,
     *
     * @param type
     * @return
     */
    fun determineIndex(type: EditorType): Int {
        val size = this.editorSettings.parentView!!.childCount
        if (this.editorSettings.renderType === RenderType.RENDERER) {
            return size
        }

        val view = this.editorSettings.activeView ?: return size
        val currentIndex = this.editorSettings.parentView!!.indexOfChild(view)
        val tag = getControlType(view)

        if (tag === EditorType.INPUT) {
            val length = (this.editorSettings.activeView as EditText).text.length
            return if (length > 0) {
                if (type === EditorType.UL_LI || type === EditorType.OL_LI) currentIndex else currentIndex
            } else {
                currentIndex
            }
        }
        return size
    }

    fun getControlType(view: View?): EditorType? = view?.let { (it.tag as ControlMetadata).type }

    fun getControlMetadata(view: View): ControlMetadata = view.tag as ControlMetadata

    private fun deleteFocusedPrevious(view: EditText) {
        val index = this.editorSettings.parentView!!.indexOfChild(view)
        if (index == 0) {
            return
        }

        removeParent(view)
    }

    fun getStateFromString(content: String?): EditorContent {
        var strContent = content

        if (strContent == null) {
            strContent = getValue("editorState", "")
        }
        return editorSettings.gson.fromJson(strContent, EditorContent::class.java)
    }

    @Suppress("SameParameterValue")
    private fun getValue(key: String, defaultVal: String): String? =
        PreferenceManager.getDefaultSharedPreferences(this.context).getString(key, defaultVal)

    private fun getNodeInstance(view: View): Node {
        val node = Node()
        val type = getControlType(view)
        node.type = type
        node.content = ArrayList()
        return node
    }

    fun isLastRow(view: View): Boolean {
        val index = this.editorSettings.parentView!!.indexOfChild(view)
        val length = this.editorSettings.parentView!!.childCount
        return length - 1 == index
    }


    @SuppressLint("SetTextI18n")
    open fun onKey(v: View, keyCode: Int, event: KeyEvent, editText: CustomEditText): Boolean {
        if (keyCode != KeyEvent.KEYCODE_DEL) {
            return false
        }
        if (inputExtensions!!.isEditTextEmpty(editText)) {
            deleteFocusedPrevious(editText)
            val controlCount = parentChildCount
            return if (controlCount == 1) checkLastControl() else false
        }
        val length = editText.text?.length ?: 0
        val selectionStart = editText.selectionStart

        val nextFocus: CustomEditText?
        if (selectionStart == 0 && length > 0) {
            val index = parentView!!.indexOfChild(editText)
            if (index == 0)
                return false
            nextFocus = inputExtensions!!.getEditTextPrevious(index)

            if (nextFocus != null) {
                deleteFocusedPrevious(editText)
                nextFocus.setText("${nextFocus.text}${editText.text}")
                nextFocus.setSelection(nextFocus.text?.length ?: 0)
            }
        }
        return false
    }

    private fun checkLastControl(): Boolean {
        val control = getControlMetadata(parentView!!.getChildAt(0))
        when (control.type) {
            EditorType.UL, EditorType.OL -> this.editorSettings.parentView!!.removeAllViews()
            else -> {}
        }

        return false
    }

    private fun onViewTouched(motionEvent: MotionEvent) {
        var position = -1
        for (i in 0 until childCount) {
            val withinBound = isViewInBounds(getChildAt(i), motionEvent.x, motionEvent.y)
            if (withinBound) {
                position = i
            }
        }

        if (position == -1) {
            var doInsert = true
            if (getControlType(getChildAt(childCount - 1)) === EditorType.INPUT) {
                val editText = getChildAt(childCount - 1) as CustomEditText
                if (TextUtils.isEmpty(editText.text)) {
                    doInsert = false
                }
            }
            if (doInsert)
                inputExtensions!!.insertEditText(childCount, null)
        }
    }

    private fun serializeContent(_state: EditorContent?): String {
        return editorSettings.gson.toJson(_state)
    }

    private fun parseHtml(htmlString: String) {
        val doc = Jsoup.parse(htmlString)
        for (element in doc.body().children()) {
            if (!HTMLExtensions.matchesTag(element.tagName().toLowerCase(Locale.ROOT))) {
                val tag = element.attr("data-tag")
                if (tag != "macro") {
                    continue
                }
            }
            buildNodeFromHTML(element)
        }
    }

    private fun removeParent(view: View): Int {
        val indexOfDeleteItem = this.editorSettings.parentView!!.indexOfChild(view)
        var nextItem: View? = null
        var nextFocusIndex = -1

        //remove hr if its on top of the delete field
        this.editorSettings.parentView!!.removeView(view)
        Log.d("indexOfDeleteItem", "indexOfDeleteItem : $indexOfDeleteItem")
        for (i in 0 until indexOfDeleteItem) {
            if (getControlType(this.editorSettings.parentView!!.getChildAt(i)) === EditorType.INPUT) {
                nextItem = this.editorSettings.parentView!!.getChildAt(i)
                nextFocusIndex = i
                continue
            }
        }

        dividerExtensions!!.removeAllDividersBetweenDeletedAndFocusNext(indexOfDeleteItem, nextFocusIndex)


        if (nextItem != null) {
            val text = nextItem as CustomEditText?
            if (text!!.requestFocus()) {
                text.setSelection(text.text?.length ?: 0)
            }
            this.editorSettings.activeView = nextItem
        }
        return indexOfDeleteItem
    }
}
