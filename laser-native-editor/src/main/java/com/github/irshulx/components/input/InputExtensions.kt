package com.github.irshulx.components.input

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.*
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.github.irshulx.EditorComponent
import com.github.irshulx.EditorCore
import com.github.irshulx.R
import com.github.irshulx.components.ComponentsWrapper
import com.github.irshulx.components.ListItemExtensions
import com.github.irshulx.components.input.edit_text.CustomEditText
import com.github.irshulx.components.input.spans.calculators.ColorSpansCalculator
import com.github.irshulx.components.input.spans.calculators.CreateSpanOperation
import com.github.irshulx.components.input.spans.calculators.DeleteSpanOperation
import com.github.irshulx.components.input.spans.calculators.StyleSpansCalculator
import com.github.irshulx.components.input.spans.custom.LinkSpan
import com.github.irshulx.components.input.spans.custom.MentionSpan
import com.github.irshulx.components.input.spans.custom.TagSpan
import com.github.irshulx.components.input.spans.spans_worker.SpansWorkerImpl
import com.github.irshulx.models.*
import com.github.irshulx.models.control_metadata.InputMetadata
import com.github.irshulx.utilities.FontCache
import com.github.irshulx.utilities.MaterialColor
import com.github.irshulx.utilities.Utilities
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.*
import java.util.regex.Pattern
import kotlin.reflect.KClass

class InputExtensions(internal var editorCore: EditorCore) : EditorComponent(editorCore) {
    var defaultTextColor = "#000000"
    var h1TextSize = 23
    var h2TextSize = 20
    var h3TextSize = 18
    var normalTextSize = 16
    private var fontFace = R.string.fontFamily__serif
    var contentTypeface: Map<Int, String>? = null
    var headingTypeface: Map<Int, String>? = null
    private var lineSpacing = -1f

    @ColorInt
    private val specialSpansColors = Color.BLUE

    private val editor: CustomEditText
        get() = editorCore.activeView as CustomEditText

    fun getFontFace(): String {
        return editorCore.context.resources.getString(fontFace)
    }

    fun setFontFace(fontFace: Int) {
        this.fontFace = fontFace
    }

    override fun getContent(view: View): Node {
        val node = this.getNodeInstance(view)
        val tag = view.getTag() as InputMetadata
        node.content!!.add(Html.toHtml((view as EditText).text))
        return node
    }

    override fun getContentAsHTML(node: Node, content: EditorContent): String {
        return getInputHtml(node)
    }

    override fun renderEditorFromState(node: Node, content: EditorContent) {
        val text = node.content!![0]
        val view = insertEditText(editorCore.childCount, editorCore.placeHolder, text)
        applyTextSettings(node, view)
    }

    override fun buildNodeFromHTML(element: Element): Node? {
        val text: String
        val count: Int
        val tv: TextView
        val tag = HtmlTag.valueOf(element.tagName().toLowerCase())
        when (tag) {
            HtmlTag.p, HtmlTag.div -> {
                text = element.html()
                count = editorCore.parentView!!.childCount
                tv = insertEditText(count, null, text)
                applyStyles(tv, element)
            }

            else -> {}
        }
        return null
    }

    override fun init(componentsWrapper: ComponentsWrapper) {
        this.componentsWrapper = componentsWrapper
    }

    internal fun GetSanitizedHtml(text: CharSequence): CharSequence {
        val spanned = Html.fromHtml(text.toString())
        return noTrailingwhiteLines(spanned)
    }

    fun setText(textView: TextView, text: CharSequence) {
        val toReplace = GetSanitizedHtml(text)
        textView.text = toReplace
    }

    fun insertTag(tagText: String) = insertTag(tagText, true)

    fun insertMention(userName: String) = insertMention(userName, true)

    fun insertLinkInText(text: String, url: String) = insertLinkInText(text, url, true)

    fun editTag(tagText: String) {
        if(removeSpecialSpan(TagSpan::class)) {
            editor.post {
                insertTag(tagText, false)
            }
        }
    }

    fun editMention(userName: String) {
        if(removeSpecialSpan(MentionSpan::class)) {
            editor.post {
                insertMention(userName, false)
            }
        }
    }

    fun editLinkInText(text: String, url: String) {
        if(removeSpecialSpan(LinkSpan::class)) {
            editor.post {
                insertLinkInText(text, url, false)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun updateTextColor(color: MaterialColor, editText: TextView?) {
        try {
            val editTextLocal = (editText ?: editor) as CustomEditText

            // Process the operation only if a selection area exists
            editTextLocal.selectionArea?.let { selection ->
                val spansWorker = SpansWorkerImpl(editTextLocal.text)

                val calculator = ColorSpansCalculator(spansWorker)
                val spanOperations = calculator.calculate(selection, MaterialColor.toSystemColor(color, editTextLocal.context))

                spanOperations.forEach { operation ->
                    when(operation) {
                        is DeleteSpanOperation -> spansWorker.removeSpan(operation.span)

                        is CreateSpanOperation<*> -> {
                            with((operation as CreateSpanOperation<Int>).spanInfo) {
                                spansWorker.createSpan(ForegroundColorSpan(value), area)
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun setLineSpacing(textView: TextView, lineHeight: Float) {
        val fontHeight = textView.paint.getFontMetricsInt(null)
        textView.setLineSpacing((Utilities.dpToPx(editorCore.context, lineHeight) - fontHeight).toFloat(), 1f)
    }

    private fun getNewEditTextInst(hint: String?, text: CharSequence?): CustomEditText {
        val editText = CustomEditText(ContextThemeWrapper(this.editorCore.context, R.style.WysiwygEditText))
        addEditableStyling(editText)
        editText.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        if (hint != null) {
            editText.hint = hint
        }
        if (text != null) {
            setText(editText, text)
        }

        /**
         * create tag for the editor
         */
        val metadata = InputMetadata(EditorType.INPUT)
        editText.tag = metadata

        editText.setBackgroundDrawable(ContextCompat.getDrawable(this.editorCore.context, R.drawable.invisible_edit_text))

        editText.setOnKeyListener { v, keyCode, event ->
            editorCore.onKey(v, keyCode, event, editText)
        }

        editText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                editText.clearFocus()
            } else {
                editorCore.activeView = v
            }
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                val spansWorker = SpansWorkerImpl(s)

                // If a span is edited by user we should remove it
                val tagsSpans = spansWorker.getSpans<TagSpan>(TagSpan::class, start..start+count)
                tagsSpans.forEach { spansWorker.removeSpan(it) }

                val mentionsSpans = spansWorker.getSpans<MentionSpan>(MentionSpan::class, start..start+count)
                mentionsSpans.forEach { spansWorker.removeSpan(it) }

                val linksSpans = spansWorker.getSpans<LinkSpan>(LinkSpan::class, start..start+count)
                linksSpans.forEach { spansWorker.removeSpan(it) }
            }

            override fun afterTextChanged(s: Editable) {

                var text = Html.toHtml(editText.text)
                val tag = editText.getTag(R.id.control_tag)
                if (s.length == 0 && tag != null)
                    editText.hint = tag.toString()
                if (s.length > 0) {
                    /*
                     * if user had pressed enter, replace it with br
                     */
                    for (i in 0 until s.length) {
                        if (s[i] == '\n') {
                            val subChars = s.subSequence(0, i)
                            val ssb = SpannableStringBuilder(subChars)
                            text = Html.toHtml(ssb)
                            if (text.length > 0)
                                setText(editText, text)


                            if (i + 1 == s.length) {
                                s.clear()
                            }

                            val index = editorCore.parentView!!.indexOfChild(editText)
                            /* if the index was 0, set the placeholder to empty, behaviour happens when the user just press enter
                             */
                            if (index == 0) {
                                editText.hint = null
                                editText.setTag(R.id.control_tag, hint)
                            }
                            val position = index + 1
                            var newText: CharSequence? = null
                            val editable = SpannableStringBuilder()
                            val lastIndex = s.length
                            val nextIndex = i + 1
                            if (nextIndex < lastIndex) {
                                newText = s.subSequence(nextIndex, lastIndex)
                                for (j in 0 until newText.length) {
                                    editable.append(newText[j])
                                    if (newText[j] == '\n') {
                                        editable.append('\n')
                                    }
                                }
                            }
                            insertEditText(position, hint, editable)
                            break
                        }
                    }
                }
                if (editorCore.editorListener != null) {
                    editorCore.editorListener!!.onTextChanged(editText, s)
                }
            }
        })
        if (this.lineSpacing != -1f) {
            setLineSpacing(editText, this.lineSpacing)
        }
        return editText
    }

    private fun isLastText(index: Int): Boolean {
        if (index == 0)
            return false
        val view = editorCore.parentView!!.getChildAt(index - 1)
        val type = editorCore.getControlType(view)
        return type === EditorType.INPUT
    }

    private fun addEditableStyling(editText: TextView) {
        editText.typeface = getTypeface(Typeface.NORMAL)
        editText.isFocusableInTouchMode = true
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, normalTextSize.toFloat())
        editText.setTextColor(Color.parseColor(this.defaultTextColor))
        editText.setPadding(0, 30, 0, 30)
    }


    fun insertEditText(position: Int, hint: String?, text: CharSequence?): TextView {
        val nextHint = if (isLastText(position)) null else editorCore.placeHolder
        if (editorCore.renderType === RenderType.EDITOR) {

            /**
             * when user press enter from first line without keyin anything, need to remove the placeholder from that line 0...
             */
            if (position == 1) {
                val view = editorCore.parentView!!.getChildAt(0)
                val type = editorCore.getControlType(view)
                if (type === EditorType.INPUT) {
                    val textView = view as TextView
                    if (TextUtils.isEmpty(textView.text)) {
                        textView.hint = null
                    }
                }
            }

            val view = getNewEditTextInst(nextHint, text)
            editorCore.parentView!!.addView(view, position)
            editorCore.activeView = view
            val handler = Handler()
            handler.postDelayed({
                setFocus(view)
            }, 0)
            editorCore.activeView = view
            return view
        } else {
            val view = getNewTextView(text)
            view.tag = InputMetadata(EditorType.INPUT)
            editorCore.parentView!!.addView(view)
            return view
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun updateTextStyle(style: EditorTextStyle, editText: TextView?) {
        try {
            val editTextLocal = (editText ?: editor) as CustomEditText

            // Process the operation only if a selection area exists
            editTextLocal.selectionArea?.let { selection ->
                val spansWorker = SpansWorkerImpl(editTextLocal.text)

                val calculator = StyleSpansCalculator(spansWorker)
                val spanOperations = calculator.calculate(selection, style)

                spanOperations.forEach { operation ->
                    when(operation) {
                        is DeleteSpanOperation -> spansWorker.removeSpan(operation.span)

                        is CreateSpanOperation<*> -> {
                            with((operation as CreateSpanOperation<EditorTextStyle>).spanInfo) {
                                spansWorker.createSpan(StyleSpan(styleToTypeface(value)), area)
                            }
                        }
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun styleToTypeface(style: EditorTextStyle): Int =
        when(style) {
            EditorTextStyle.ITALIC -> Typeface.ITALIC
            EditorTextStyle.BOLD -> Typeface.BOLD
            EditorTextStyle.BOLD_ITALIC -> Typeface.BOLD_ITALIC
            else -> throw UnsupportedOperationException("This style is not supported: $style")
        }

    fun insertLink() {
        val inputAlert = AlertDialog.Builder(this.editorCore.context)
        inputAlert.setTitle("Add a Link")
        val userInput = EditText(this.editorCore.context)
        //dont forget to add some margins on the left and right to match the title
        userInput.hint = "type the URL here"
        userInput.inputType = InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT
        inputAlert.setView(userInput)
        inputAlert.setPositiveButton("INSERT") { dialog, which ->
            val userInputValue = userInput.text.toString()
            insertLink(userInputValue)
        }
        inputAlert.setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }
        val alertDialog = inputAlert.create()
        alertDialog.show()
    }

    fun appendText(text: Editable) {

    }

    fun insertLink(uri: String) {
        val editorType = editorCore.getControlType(editorCore.activeView)
        val editText = editorCore.activeView as EditText?
        if (editorType === EditorType.INPUT || editorType === EditorType.UL_LI) {
            var text = Html.toHtml(editText!!.text)
            if (TextUtils.isEmpty(text))
                text = "<p dir=\"ltr\"></p>"
            text = trimLineEnding(text)
            val _doc = Jsoup.parse(text)
            val x = _doc.select("p")
            val existing = x[0].html()
            x[0].html("$existing <a href='$uri'>$uri</a>")
            val toTrim = Html.fromHtml(x.toString())
            val trimmed = noTrailingwhiteLines(toTrim)
            editText.setText(trimmed)   //
            editText.setSelection(editText.text.length)
        }
    }

    fun noTrailingwhiteLines(text: CharSequence): CharSequence {
        var text = text
        if (text.length == 0)
            return text
        while (text[text.length - 1] == '\n') {
            text = text.subSequence(0, text.length - 1)
        }
        return text
    }

    fun noLeadingwhiteLines(text: CharSequence): CharSequence {
        var text = text
        if (text.length == 0)
            return text
        while (text[0] == '\n') {
            text = text.subSequence(1, text.length)
        }
        return text
    }

    fun isEditTextEmpty(editText: EditText): Boolean {
        return editText.text.toString().trim { it <= ' ' }.length == 0
    }

    private fun trimLineEnding(s: String): String {
        return if (s[s.length - 1] == '\n') {
            s.substring(0, s.length - 1)
        } else s
    }

    /**
     * returns the appropriate typeface
     *
     * @param mode  => whether heading (0) or content(1)
     * @param style => NORMAL, BOLD, BOLD_ITALIC, ITALIC
     * @return typeface
     */
    fun getTypeface(style: Int): Typeface? =
        if (contentTypeface == null) {
            Typeface.create(getFontFace(), style)
        } else {
            FontCache[contentTypeface!![style]!!, editorCore.context]
        }

    private fun setFocus(view: CustomEditText) {
        if (editorCore.isStateFresh && !editorCore.autoFucus) {
            editorCore.isStateFresh = false
            return
        }
        view.requestFocus()
        val mgr = editorCore.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        view.setSelection(view.text?.length ?: 0)
        editorCore.activeView = view
    }


    fun getEditTextPrevious(startIndex: Int): CustomEditText? {
        var customEditText: CustomEditText? = null
        for (i in 0 until startIndex) {
            val view = editorCore.parentView!!.getChildAt(i)
            val editorType = editorCore.getControlType(view)
            if (editorType === EditorType.HR || editorType === EditorType.IMG || editorType === EditorType.MAP)
                continue
            if (editorType === EditorType.INPUT) {
                customEditText = view as CustomEditText
                continue
            }
            if (editorType === EditorType.OL || editorType === EditorType.UL) {
                componentsWrapper!!.listItemExtensions!!.setFocusToList(view, ListItemExtensions.POSITION_START)
                editorCore.activeView = view
            }
        }
        return customEditText
    }

    fun setFocusToPrevious(startIndex: Int) {
        for (i in startIndex downTo 1) {
            val view = editorCore.parentView!!.getChildAt(i)
            val editorType = editorCore.getControlType(view)
            if (editorType === EditorType.HR || editorType === EditorType.IMG || editorType === EditorType.MAP)
                continue
            if (editorType === EditorType.INPUT) {
                setFocus(view as CustomEditText)
                break
            }
            if (editorType === EditorType.OL || editorType === EditorType.UL) {
                componentsWrapper!!.listItemExtensions!!.setFocusToList(view, ListItemExtensions.POSITION_START)
                editorCore.activeView = view
            }
        }
    }

    fun isInputTextAtPosition(position: Int): Boolean {
        return editorCore.getControlType(editorCore.parentView!!.getChildAt(position)) === EditorType.INPUT
    }

    fun applyStyles(editText: TextView, element: Element) {
        val styles = componentsWrapper!!.htmlExtensions!!.getStyleMap(element)
        if (styles.containsKey("color")) {
            updateTextColor(MaterialColor.BLACK, editText)
        }
    }

    fun getInputHtml(item: Node): String {
        return ""
//        var isParagraph = true
//        var tmpl = componentsWrapper!!.htmlExtensions!!.getTemplateHtml(item.type!!)
//        //  CharSequence content= android.text.Html.fromHtml(item.content.get(0)).toString();
//        //  CharSequence trimmed= editorCore.getInputExtensions().noTrailingwhiteLines(content);
//        val trimmed = Jsoup.parse(item.content!![0]).body().select("p").html()
//        val styles = HashMap<Enum<*>, String>()
//        if (item.styleSpans!!.size > 0) {
//            for (style in item.styleSpans!!) {
//                when (style) {
//                    EditorTextStyle.BOLD -> tmpl = tmpl.replace("{{\$content}}", "<b>{{\$content}}</b>")
//                    EditorTextStyle.BOLD_ITALIC -> tmpl = tmpl.replace("{{\$content}}", "<b><i>{{\$content}}</i></b>")
//                    EditorTextStyle.ITALIC -> tmpl = tmpl.replace("{{\$content}}", "<i>{{\$content}}</i>")
//                    EditorTextStyle.NORMAL -> {
//                        tmpl = tmpl.replace("{{\$tag}}", "p")
//                        isParagraph = true
//                    }
//                }
//            }
//        }
//
//        styles[TEXT_COLOR] = "color:" + item.colorSpans!!.textColor!!
//
//        if (item.type === EditorType.OL_LI || item.type === EditorType.UL_LI) {
//            tmpl = tmpl.replace("{{\$tag}}", "span")
//        } else if (isParagraph) {
//            tmpl = tmpl.replace("{{\$tag}}", "p")
//        }
//        tmpl = tmpl.replace("{{\$content}}", trimmed)
//        tmpl = tmpl.replace(" {{\$style}}", createStyleTag(styles))
//        return tmpl
    }

    private fun createStyleTag(styles: Map<Enum<*>, String>): String {
        var tmpl = " style=\"{{builder}}\""

        val builder = StringBuilder()
        for ((_, value) in styles) {
            builder.append(value).append(";")
        }
        tmpl = tmpl.replace("{{builder}}", builder.toString())
        return tmpl
    }

    fun applyTextSettings(node: Node, view: TextView) {
//        if (node.styleSpans != null) {
//            for (style in node.styleSpans!!) {
//                updateTextStyle(style, view)
//            }
//
//            if (!TextUtils.isEmpty(node.colorSpans!!.textColor)) {
//                updateTextColor(node.colorSpans!!.textColor!!, view)
//            }
//        }
    }

    fun removeFocus(editText: CustomEditText) {
        editText.clearFocus()
        val imm = editorCore.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
        editorCore.parentView!!.removeView(editText)
    }

    fun setLineSpacing(lineSpacing: Float) {
        this.lineSpacing = lineSpacing
    }

    private fun getNewTextView(text: CharSequence?): TextView {
        val textView = TextView(ContextThemeWrapper(this.editorCore.context, R.style.WysiwygEditText))
        addEditableStyling(textView)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textView.layoutParams = params

        if (!TextUtils.isEmpty(text)) {
            val text = Html.fromHtml(text.toString())
            val toReplace = noTrailingwhiteLines(text)
            textView.text = toReplace
            Linkify.addLinks(textView, Linkify.ALL)
        }

        if (this.lineSpacing != -1f) {
            setLineSpacing(textView, this.lineSpacing)
        }
        return textView
    }

    private fun translateColor(color: String): String {
        if (color.contains("rgb")) {
            val c = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)")
            val m = c.matcher(color)
            if (m.matches()) {
                val r = Integer.parseInt(m.group(1))
                val g = Integer.parseInt(m.group(2))
                val b = Integer.parseInt(m.group(3))
                return String.format(Locale.getDefault(), "#%02X%02X%02X", r, g, b)
            }
        }
        return color
    }

    private fun insertTag(tagText: String, addSpace: Boolean) =
        insertSpecialSpan("#$tagText", TagSpan(tagText, specialSpansColors), addSpace)

    private fun insertMention(userName: String, addSpace: Boolean) =
        insertSpecialSpan("@$userName", MentionSpan(userName, specialSpansColors), addSpace)

    private fun insertLinkInText(text: String, url: String, addSpace: Boolean) =
        insertSpecialSpan(text, LinkSpan(url, specialSpansColors), addSpace)

    private fun insertSpecialSpan(textToDisplay: String, span: CharacterStyle, addSpace: Boolean) {
        try {
            if(editor.selectionArea != null) {
                return
            }

            editor.text?.let { textArea ->
                val spansWorker = SpansWorkerImpl(textArea)

                val startPosition = editor.cursorPosition
                val endPosition = editor.cursorPosition+textToDisplay.length

                val suffix = if(addSpace) " " else ""

                textArea.insert(editor.cursorPosition, "$textToDisplay$suffix")

                editor.post {
                    spansWorker.createSpan(span, startPosition..endPosition)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * Removes special span under a cursor position
     */
    private fun removeSpecialSpan(spanType: KClass<*>): Boolean {
        try {
            if(editor.selectionArea != null) {
                return false
            }

            editor.text?.let { textArea ->
                val spansWorker = SpansWorkerImpl(textArea)

                spansWorker.getSpanUnderPosition(spanType, editor.cursorPosition)
                    ?.let { span ->
                        val spanInterval = spansWorker.getSpanInterval(span)

                        spansWorker.removeSpan(span)

                        editor.post {
                            textArea.delete(spanInterval.first, spanInterval.last)
                        }

                        return true
                    }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return false
    }
}
