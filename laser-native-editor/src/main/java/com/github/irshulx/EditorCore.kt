package com.github.irshulx

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
import com.github.irshulx.models.control_metadata.InputMetadata
import com.github.irshulx.models.control_metadata.ListItemMetadata
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.*

open class EditorCore(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), View.OnTouchListener {
    companion object {
        val TAG = "EDITOR"
    }

    open var editorListener: EditorListener? = null
    private val MAP_MARKER_REQUEST = 20
    val PICK_IMAGE_REQUEST = 1

    /*
     * Getters and setters for  extensions
     */
    protected var inputExtensions: InputExtensions? = null
        private set

    protected var imageExtensions: ImageExtensions? = null
        private set

    protected var listItemExtensions: ListItemExtensions? = null
        private set

    protected var dividerExtensions: DividerExtensions? = null
        private set

    protected var htmlExtensions: HTMLExtensions? = null
        private set

    protected var mapExtensions: MapExtensions? = null
        private set

    protected var macroExtensions: MacroExtensions? = null
        private set

    private val editorSettings: EditorSettings

    private var componentsWrapper: ComponentsWrapper? = null


    //region Getters_and_Setters

    /**
     * Exposed
     */

    /**
     * returns activity
     *
     * @return
     */
    val activity: Activity
        get() = this.editorSettings.context as Activity

    /**
     * used to get the editor node
     *
     * @return
     */
    val parentView: LinearLayout?
        get() = this.editorSettings.parentView

    /**
     * Get number of childs in the editor
     *
     * @return
     */
    val parentChildCount: Int
        get() = this.editorSettings.parentView!!.childCount

    /**
     * returns whether editor is set as Editor or Rendeder
     *
     * @return
     */
    val renderType: RenderType?
        get() = this.editorSettings.renderType

    /**
     * The current active view on the editor
     *
     * @return
     */
    var activeView: View?
        get() = this.editorSettings.activeView
        set(view) {
            this.editorSettings.activeView = view
        }
    //endregion


    /*
    Used by Editor
     */
    protected open val contentAsSerialized: String
        get() {
            val state = content
            return serializeContent(state)
        }


    protected open//field type, content[]
    val content: EditorContent?
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
                    EditorType.UL, EditorType.OL -> {
                        node = listItemExtensions!!.getContent(view)
                        list.add(node)
                    }
                    EditorType.MAP -> {
                        node = mapExtensions!!.getContent(view)
                        list.add(node)
                    }
                    EditorType.MACRO -> {
                        node = macroExtensions!!.getContent(view)
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

    var isSerialRenderInProgress: Boolean
        get() = this.editorSettings.serialRenderInProgress
        set(serialRenderInProgress) {
            this.editorSettings.serialRenderInProgress = serialRenderInProgress
        }


    val placeHolder: String?
        get() = editorSettings.placeHolder

    val autoFucus: Boolean
        get() = editorSettings.autoFocus

    init {
        editorSettings = EditorSettings.init(context, this)
        this.orientation = LinearLayout.VERTICAL
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
        listItemExtensions = ListItemExtensions(this)
        dividerExtensions = DividerExtensions(this)
        mapExtensions = MapExtensions(this)
        htmlExtensions = HTMLExtensions(this)
        macroExtensions = MacroExtensions(this)

        componentsWrapper = ComponentsWrapper.Builder()
                .inputExtensions(inputExtensions!!)
                .htmlExtensions(htmlExtensions!!)
                .dividerExtensions(dividerExtensions!!)
                .imageExtensions(imageExtensions!!)
                .listItemExtensions(listItemExtensions!!)
                .macroExtensions(macroExtensions!!)
                .mapExtensions(mapExtensions!!)
                .build()

        macroExtensions!!.init(componentsWrapper!!)
        dividerExtensions!!.init(componentsWrapper!!)
        inputExtensions!!.init(componentsWrapper!!)
        imageExtensions!!.init(componentsWrapper!!)
        listItemExtensions!!.init(componentsWrapper!!)
        mapExtensions!!.init(componentsWrapper!!)
    }


    fun ___onViewTouched(hotspot: Int, viewPosition: Int) {
        if (hotspot == 0) {
            if (!inputExtensions!!.isInputTextAtPosition(viewPosition - 1)) {
                inputExtensions!!.insertEditText(viewPosition, null, null)
            } else {
                Log.d(TAG, "not adding another edittext since already an edittext on the top")
            }
        } else if (hotspot == 1) {
            if (!inputExtensions!!.isInputTextAtPosition(viewPosition + 1)) {
                inputExtensions!!.insertEditText(viewPosition + 1, null, null)
            } else {
                Log.d(TAG, "not adding another edittext since already an edittext below")
            }
        }
    }

    fun ___onViewTouched(view: View, motionEvent: MotionEvent) {
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
                inputExtensions!!.insertEditText(childCount, null, null)
        }
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

    protected fun serializeContent(_state: EditorContent?): String {
        return editorSettings.gson.toJson(_state)
    }


    protected fun renderEditor(_state: EditorContent) {
        this.editorSettings.parentView!!.removeAllViews()
        this.editorSettings.serialRenderInProgress = true
        for (item in _state.nodes!!) {
            when (item.type) {
                EditorType.INPUT -> inputExtensions!!.renderEditorFromState(item, _state)
                EditorType.HR -> dividerExtensions!!.renderEditorFromState(item, _state)
                EditorType.IMG -> imageExtensions!!.renderEditorFromState(item, _state)
                EditorType.UL, EditorType.OL -> listItemExtensions!!.renderEditorFromState(item, _state)
                EditorType.MAP -> mapExtensions!!.renderEditorFromState(item, _state)
                EditorType.MACRO -> macroExtensions!!.renderEditorFromState(item, _state)
                else -> {}
            }
        }
        this.editorSettings.serialRenderInProgress = false
    }

    protected fun parseHtml(htmlString: String) {
        val doc = Jsoup.parse(htmlString)
        for (element in doc.body().children()) {
            if (!HTMLExtensions.matchesTag(element.tagName().toLowerCase())) {
                val tag = element.attr("data-tag")
                if (tag != "macro") {
                    continue
                }
            }
            buildNodeFromHTML(element)
        }
    }

    private fun buildNodeFromHTML(element: Element) {
        val text: String

        val macroTag = element.attr("data-tag")
        if (macroTag == "macro") {
            macroExtensions!!.buildNodeFromHTML(element)
            return
        }

        val tag = HtmlTag.valueOf(element.tagName().toLowerCase())
        val count = parentView!!.childCount

        if ("br" == tag.name || "<br>" == element.html().replace("\\s+".toRegex(), "") || "<br/>" == element.html().replace("\\s+".toRegex(), "")) {
            inputExtensions!!.insertEditText(count, null, null)
            return
        } else if ("hr" == tag.name || "<hr>" == element.html().replace("\\s+".toRegex(), "") || "<hr/>" == element.html().replace("\\s+".toRegex(), "")) {
            dividerExtensions!!.buildNodeFromHTML(element)
            return
        }

        when (tag) {
            HtmlTag.h1, HtmlTag.h2, HtmlTag.h3, HtmlTag.p, HtmlTag.blockquote -> inputExtensions!!.buildNodeFromHTML(element)
            HtmlTag.ul, HtmlTag.ol -> listItemExtensions!!.buildNodeFromHTML(element)
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
                EditorType.MAP -> {
                    val htmlMap = mapExtensions!!.getContentAsHTML(item, content)
                    htmlBlock.append(htmlMap)
                }
                EditorType.UL, EditorType.OL -> htmlBlock.append(listItemExtensions!!.getContentAsHTML(item, content))
                EditorType.MACRO -> htmlBlock.append(macroExtensions!!.getContentAsHTML(item, content))

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
                this.editorSettings.renderType = com.github.irshulx.models.RenderType.EDITOR
            } else {
                this.editorSettings.renderType = if (renderType!!.toLowerCase() == "renderer") RenderType.RENDERER else RenderType.EDITOR
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
        if (this.editorSettings.renderType === RenderType.RENDERER)
            return size
        val _view = this.editorSettings.activeView ?: return size
        val currentIndex = this.editorSettings.parentView!!.indexOfChild(_view)
        val tag = getControlType(_view)
        if (tag === EditorType.INPUT) {
            val length = (this.editorSettings.activeView as EditText).text.length
            return if (length > 0) {
                if (type === EditorType.UL_LI || type === EditorType.OL_LI) currentIndex else currentIndex
            } else {
                currentIndex
            }
        } else if (tag === EditorType.UL_LI || tag === EditorType.OL_LI) {
            val _text = _view.findViewById<EditText>(R.id.txtText)
            if (_text.text.length > 0) {

            }
            return size
        } else {
            return size
        }
    }

    fun containsStyle(_Styles: List<EditorTextStyle>, style: EditorTextStyle): Boolean {
        for (item in _Styles) {
            if (item === style) {
                return true
            }
            continue
        }
        return false
    }

    fun updateMetadataStyle(metadata: InputMetadata, style: EditorTextStyle, _operation: Operation): InputMetadata {
        val styles = metadata.editorTextStyles
        if (_operation === Operation.DELETE) {
            val index = styles.indexOf(style)
            if (index != -1) {
                styles.removeAt(index)
                metadata.editorTextStyles = styles
            }
        } else {
            val index = styles.indexOf(style)
            if (index == -1) {
                styles.add(style)
            }
        }
        return metadata
    }

    fun getControlType(view: View?): EditorType? = view?.let { (it.tag as ControlMetadata).type }

    fun getControlMetadata(view: View): ControlMetadata = view.tag as ControlMetadata

    private fun deleteFocusedPrevious(view: EditText) {
        val index = this.editorSettings.parentView!!.indexOfChild(view)
        if (index == 0) {
            return
        }

        // If the person was on an active ul|li, move him to the previous node
        ((view.parent as View).tag as? ControlMetadata)?.let { contentType ->
            if (contentType.type === EditorType.OL_LI || contentType.type === EditorType.UL_LI) {
                listItemExtensions!!.validateAndRemoveLisNode(view, contentType as ListItemMetadata)
                return@deleteFocusedPrevious
            }
        }

        val toFocus = this.editorSettings.parentView!!.getChildAt(index - 1)
        val control = toFocus.tag as ControlMetadata

         // If its an image or map, do not delete edittext, as there is nothing to focus on after image
         // If the person was on edittext,  had removed the whole text, we need to move into the previous line
        if (control.type === EditorType.OL || control.type === EditorType.UL) {
            this.editorSettings.parentView!!.removeView(view)   // previous node on the editor is a list, set focus to its inside
            listItemExtensions!!.setFocusToList(toFocus, ListItemExtensions.POSITION_END)
        } else {
            removeParent(view)
        }
    }


    fun removeParent(view: View): Int {
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


    fun getStateFromString(content: String?): EditorContent {
        var content = content
        if (content == null) {
            content = getValue("editorState", "")
        }
        return editorSettings.gson.fromJson(content, EditorContent::class.java)
    }

    private fun getValue(Key: String, defaultVal: String): String? {
        val _Preferences = PreferenceManager.getDefaultSharedPreferences(this.context)
        return _Preferences.getString(Key, defaultVal)

    }

    protected fun putValue(Key: String, Value: String) {
        val _Preferences = PreferenceManager.getDefaultSharedPreferences(this.context)
        val editor = _Preferences.edit()
        editor.putString(Key, Value)
        editor.apply()
    }


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

        val editorType = getControlType(this.editorSettings.activeView)
        val nextFocus: CustomEditText?
        if (selectionStart == 0 && length > 0) {
            if (editorType === EditorType.UL_LI || editorType === EditorType.OL_LI) {
                //now that we are inside the edittext, focus inside it
                val index = listItemExtensions!!.getIndexOnEditorByEditText(editText)
                if (index == 0) {
                    deleteFocusedPrevious(editText)
                }
            } else {
                val index = parentView!!.indexOfChild(editText)
                if (index == 0)
                    return false
                nextFocus = inputExtensions!!.getEditTextPrevious(index)

                if (nextFocus != null) {
                    deleteFocusedPrevious(editText)
                    nextFocus.setText(nextFocus.text.toString() + editText.text.toString())
                    nextFocus.setSelection(nextFocus.text?.length ?: 0)
                }
            }
        }
        return false
    }

    private fun checkLastControl(): Boolean {
        val control = getControlMetadata(parentView!!.getChildAt(0)) ?: return false
        when (control.type) {
            EditorType.UL, EditorType.OL -> this.editorSettings.parentView!!.removeAllViews()
        }

        return false
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        ___onViewTouched(view, motionEvent)
        return false
    }
}
