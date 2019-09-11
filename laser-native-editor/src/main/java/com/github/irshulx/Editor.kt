package com.github.irshulx

import android.content.Context
import android.graphics.Bitmap
import android.text.Editable
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View

import com.github.irshulx.Components.CustomEditText
import com.github.irshulx.models.EditorTextStyle
import com.github.irshulx.models.EditorContent
import com.github.irshulx.models.RenderType

class Editor(context: Context, attrs: AttributeSet) : EditorCore(context, attrs) {

    override var editorListener: EditorListener?
        get() = super.editorListener
        set(_listener) {
            super.editorListener = _listener
        }

    public override val content: EditorContent?
        get() = super.content

    public override val contentAsSerialized: String
        get() = super.contentAsSerialized

    val contentAsHTML: String
        get() = htmlContent

    //region Miscellanious getters and setters

    /**
     * Input extension
     */
    /**
     * size in sp
     * @param size
     */
    var h1TextSize: Int
        get() = inputExtensions!!.h1TextSize
        set(size) {
            inputExtensions!!.h1TextSize = size
        }

    /**
     * size in sp
     * @param size
     */
    var h2TextSize: Int
        get() = inputExtensions!!.h2TextSize
        set(size) {
            inputExtensions!!.h2TextSize = size
        }

    /**
     * size in sp
     * @param size
     */
    var h3TextSize: Int
        get() = inputExtensions!!.h3TextSize
        set(size) {
            inputExtensions!!.h3TextSize = size
        }

    /**
     * setup the fontfaces for editor content
     * For eg:
     * Map<Integer></Integer>, String> typefaceMap = new HashMap<>();
     * typefaceMap.put(Typeface.NORMAL,"fonts/GreycliffCF-Medium.ttf");
     * typefaceMap.put(Typeface.BOLD,"fonts/GreycliffCF-Bold.ttf");
     * typefaceMap.put(Typeface.ITALIC,"fonts/GreycliffCF-Medium.ttf");
     * typefaceMap.put(Typeface.BOLD_ITALIC,"fonts/GreycliffCF-Medium.ttf");
     *
     * @param map
     */

    var contentTypeface: Map<Int, String>?
        get() = inputExtensions!!.contentTypeface
        set(map) {
            inputExtensions!!.contentTypeface = map
        }

    /**
     * setup the fontfaces for editor heding tags (h1,h2,h3)
     * for Eg:
     * Map<Integer></Integer>, String> typefaceMap = new HashMap<>();
     * typefaceMap.put(Typeface.NORMAL,"fonts/GreycliffCF-Medium.ttf");
     * typefaceMap.put(Typeface.BOLD,"fonts/GreycliffCF-Bold.ttf");
     * typefaceMap.put(Typeface.ITALIC,"fonts/GreycliffCF-Medium.ttf");
     * typefaceMap.put(Typeface.BOLD_ITALIC,"fonts/GreycliffCF-Medium.ttf");
     *
     * @param map
     */
    var headingTypeface: Map<Int, String>?
        get() = inputExtensions!!.headingTypeface
        set(map) {
            inputExtensions!!.headingTypeface = map
        }

    init {
        super.editorListener = null
    }

    public override fun getContentAsSerialized(state: EditorContent): String {
        return super.getContentAsSerialized(state)
    }

    public override fun getContentDeserialized(EditorContentSerialized: String): EditorContent {
        return super.getContentDeserialized(EditorContentSerialized)
    }

    fun getContentAsHTML(content: EditorContent): String {
        return getHTMLContent(content)
    }

    fun getContentAsHTML(editorContentAsSerialized: String): String {
        return getHTMLContent(editorContentAsSerialized)
    }

    fun render(_state: EditorContent) {
        super.renderEditor(_state)
    }

    fun render(HtmlString: String) {
        renderEditorFromHtml(HtmlString)
    }

    fun render() {
        if (renderType === RenderType.EDITOR) {
            inputExtensions!!.insertEditText(0, this.placeHolder, null)
        }
    }

    private fun restoreState() {
        val state = getStateFromString(null)
        render(state)
    }

    public override fun clearAllContents() {
        super.clearAllContents()
        if (renderType === RenderType.EDITOR) {
            inputExtensions!!.insertEditText(0, this.placeHolder, null)
        }
    }

    /**
     * size in sp
     * @param size
     */
    fun setNormalTextSize(size: Int) {
        inputExtensions!!.normalTextSize = size
    }


    /**
     * set dafault text color in hex
     * @param color
     */
    fun setEditorTextColor(color: String) {
        inputExtensions!!.defaultTextColor = color
    }

    /**
     * Set the fontface for the editor
     *
     */
    @Deprecated("use {@link #setContentTypeface(Map)} and {@link #setHeadingTypeface(Map)} (Map)} ()} instead.")
    fun setFontFace(StringResource: Int) {
        inputExtensions!!.setFontFace(StringResource)
    }


    fun updateTextStyle(style: EditorTextStyle) {
        inputExtensions!!.UpdateTextStyle(style, null)
    }

    fun updateTextColor(color: String) {
        inputExtensions!!.updateTextColor(color, null)
    }

    fun insertLink() {
        inputExtensions!!.insertLink()
    }

    fun insertLink(link: String) {
        inputExtensions!!.insertLink(link)
    }

    fun appendText(text: Editable) {
        inputExtensions!!.appendText(text)
    }


    /*
         *
         * Divider extension
         *
         */

    fun setDividerLayout(layout: Int) {
        this.dividerExtensions!!.setDividerLayout(layout)
    }

    fun insertDivider() {
        dividerExtensions!!.insertDivider(-1)
    }

    /*
         *
         * Image Extension
         *
         */

    fun openImagePicker() {
        imageExtensions!!.openImageGallery()
    }

    fun insertImage(bitmap: Bitmap) {
        imageExtensions!!.insertImage(bitmap, null, -1, null, true)
    }

    fun onImageUploadComplete(url: String, imageId: String) {
        imageExtensions!!.onPostUpload(url, imageId)
    }

    fun onImageUploadFailed(imageId: String) {
        imageExtensions!!.onPostUpload(null, imageId)
    }

    /*
     *
     *List Item extension
     *
     */
    fun setListItemLayout(layout: Int) {
        this.listItemExtensions!!.setListItemTemplate(layout)
    }

    fun insertList(isOrdered: Boolean) {
        this.listItemExtensions!!.insertlist(isOrdered)
    }


    override fun onKey(v: View, keyCode: Int, event: KeyEvent, editText: CustomEditText): Boolean {
        val onKey = super.onKey(v, keyCode, event, editText)
        if (parentChildCount == 0)
            render()
        return onKey
    }

    fun setLineSpacing(lineSpacing: Float) {
        this.inputExtensions!!.setLineSpacing(lineSpacing)
    }

    fun setListItemLineSpacing(lineSpacing: Float) {
        this.listItemExtensions!!.setLineSpacing(lineSpacing)
    }

    fun insertMacro(name: String, view: View, settings: MutableMap<String, Any>) {
        this.macroExtensions!!.insertMacro(name, view, settings, -1)
    }
}
