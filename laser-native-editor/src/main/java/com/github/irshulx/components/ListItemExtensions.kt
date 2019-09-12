package com.github.irshulx.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

import com.github.irshulx.EditorComponent
import com.github.irshulx.EditorCore
import com.github.irshulx.R
import com.github.irshulx.components.input.edit_text.CustomEditText
import com.github.irshulx.models.EditorContent
import com.github.irshulx.models.EditorType
import com.github.irshulx.models.HtmlTag
import com.github.irshulx.models.Node
import com.github.irshulx.models.RenderType
import com.github.irshulx.models.TextSettings
import com.github.irshulx.models.control_metadata.ListItemMetadata

import org.jsoup.nodes.Element

import java.util.ArrayList

class ListItemExtensions(internal var editorCore: EditorCore) : EditorComponent(editorCore) {
    private var listItemTemplate = R.layout.tmpl_list_item
    private var lineSpacing = -1f

    override fun getContent(view: View): Node {
        val node = getNodeInstance(view)
        node.childs = ArrayList()
        val table = view as TableLayout
        val _rowCount = table.childCount
        for (j in 0 until _rowCount) {
            val row = table.getChildAt(j)
            val node1 = getNodeInstance(row)
            val li = row.findViewById<EditText>(R.id.txtText)
            val metadata = li.tag as ListItemMetadata
            node1.contentStyles = metadata.editorTextStyles
            node1.content!!.add(Html.toHtml(li.text))
            node1.textSettings = metadata.textSettings
            node1.content!!.add(Html.toHtml(li.text))
            node.childs!!.add(node1)
        }
        return node
    }

    override fun getContentAsHTML(node: Node, content: EditorContent): String {

        val count = node.childs!!.size
        var tmpl_parent = componentsWrapper!!.htmlExtensions!!.getTemplateHtml(node.type!!)
        val childBlock = StringBuilder()
        for (i in 0 until count) {
            val html = componentsWrapper!!.inputExtensions!!.getInputHtml(node.childs!![i])
            childBlock.append(html)
        }
        tmpl_parent = tmpl_parent.replace("{{\$content}}", childBlock.toString())
        return tmpl_parent
    }

    override fun renderEditorFromState(node: Node, content: EditorContent) {
        onRenderfromEditorState(content, node)
    }

    override fun buildNodeFromHTML(element: Element): Node? {
        val tag = HtmlTag.valueOf(element.tagName().toLowerCase())
        RenderList(tag === HtmlTag.ol, element)
        return null
    }

    override fun init(componentsWrapper: ComponentsWrapper) {
        this.componentsWrapper = componentsWrapper
    }

    fun setListItemTemplate(drawable: Int) {
        this.listItemTemplate = drawable
    }

    fun insertList(Index: Int, isOrdered: Boolean, text: String): TableLayout {

        val table = createTable()
        editorCore.parentView!!.addView(table, Index)
        table.tag = ListItemMetadata(if (isOrdered) EditorType.OL else EditorType.UL)
        addListItem(table, isOrdered, text)
        return table
    }

    fun createTable(): TableLayout {
        val table = TableLayout(editorCore.context)
        table.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        table.setPadding(30, 10, 10, 10)
        return table
    }


    fun addListItem(layout: TableLayout, isOrdered: Boolean, text: String): View {
        val childLayout = (editorCore.context as Activity).layoutInflater.inflate(this.listItemTemplate, null)
        val editText = childLayout.findViewById<CustomEditText>(R.id.txtText)
        val _order = childLayout.findViewById<View>(R.id.lblOrder) as TextView
        _order.typeface = Typeface.create(componentsWrapper!!.inputExtensions!!.getFontFace(), Typeface.BOLD)
        editText.typeface = Typeface.create(componentsWrapper!!.inputExtensions!!.getFontFace(), Typeface.NORMAL)
        if (isOrdered) {
            val count = layout.childCount
            _order.text = (count + 1).toString() + "."
        }
        if (editorCore.renderType === RenderType.EDITOR) {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, componentsWrapper!!.inputExtensions!!.normalTextSize.toFloat())
            editText.setTextColor(Color.parseColor(componentsWrapper!!.inputExtensions!!.defaultTextColor))
            if (this.lineSpacing != -1f) componentsWrapper!!.inputExtensions!!.setLineSpacing(editText, this.lineSpacing)
            val metadata = ListItemMetadata(if (isOrdered) EditorType.OL_LI else EditorType.UL_LI)
            metadata.textSettings = TextSettings(componentsWrapper!!.inputExtensions!!.defaultTextColor)
            editText.tag = metadata
            childLayout.tag = metadata
            editText.typeface = componentsWrapper!!.inputExtensions!!.getTypeface(Typeface.NORMAL)
            editorCore.activeView = editText
            componentsWrapper!!.inputExtensions!!.setText(editText, text)
            editText.setOnClickListener { v ->
                editorCore.activeView = v
                //   toggleToolbarProperties(v,null);
            }
            editText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    editorCore.activeView = v
                }
            }
            editText.setOnKeyListener { v, keyCode, event -> editorCore.onKey(v, keyCode, event, editText) }

            editText.addTextChangedListener(object : TextWatcher {

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    //                if (s.length() == 0) {
                    //                    deleteFocusedPrevious(editText);
                    //                }
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                               after: Int) {
                }

                override fun afterTextChanged(s: Editable) {
                    var text = Html.toHtml(editText.text)
                    if (s.length > 0) {
                        if (s[s.length - 1] == '\n') {
                            text = text.replace("<br>".toRegex(), "")
                            val _row = editText.parent as TableRow
                            val _table = _row.parent as TableLayout
                            val type = editorCore.getControlType(_table)
                            if (s.length == 0 || s.toString() == "\n") {
                                val index = editorCore.parentView!!.indexOfChild(_table)
                                _table.removeView(_row)
                                componentsWrapper!!.inputExtensions!!.insertEditText(index + 1, "", "")
                            } else {
                                val text = Html.fromHtml(text)
                                val toReplace = componentsWrapper!!.inputExtensions!!.noTrailingwhiteLines(text)

                                if (toReplace.isNotEmpty()) {
                                    editText.setText(toReplace)
                                } else {
                                    editText.text?.clear()
                                }

                                val index = _table.indexOfChild(_row)
                                //  insertEditText(index + 1, "");
                                addListItem(_table, type === EditorType.OL, "")
                            }

                        }
                    }
                }
            })

            val handler = Handler()
            handler.postDelayed({
                editText.requestFocus()
                val mgr = editorCore.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                mgr.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                editText.setSelection(editText.text?.length ?: 0)
            }, 0)
        } else {
            val textView = childLayout.findViewById<TextView>(R.id.lblText)
            textView.typeface = componentsWrapper!!.inputExtensions!!.getTypeface(Typeface.NORMAL)

            /*
            It's a renderer, so instead of EditText,render TextView
             */
            if (!TextUtils.isEmpty(text)) {
                componentsWrapper!!.inputExtensions!!.setText(textView, text)
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, componentsWrapper!!.inputExtensions!!.normalTextSize.toFloat())
            if (this.lineSpacing != -1f) componentsWrapper!!.inputExtensions!!.setLineSpacing(textView, this.lineSpacing)
            textView.visibility = View.VISIBLE
            Linkify.addLinks(textView, Linkify.ALL)
            editText.visibility = View.GONE
        }
        layout.addView(childLayout)
        return childLayout
    }

    fun convertListToNormalText(_table: TableLayout, startIndex: Int) {
        var tableChildCount = _table.childCount
        var i = startIndex
        while (i < tableChildCount) {
            val _childRow = _table.getChildAt(i)
            _table.removeView(_childRow)
            val text = getTextFromListItem(_childRow)
            val Index = editorCore.parentView!!.indexOfChild(_table)
            componentsWrapper!!.inputExtensions!!.insertEditText(Index + 1, "", text)
            i -= 1
            tableChildCount -= 1
            i++
        }
        //if item is the last in the table, remove the table from parent

        if (_table.childCount == 0) {
            editorCore.parentView!!.removeView(_table)
        }
    }

    @SuppressLint("SetTextI18n")
    fun convertListToOrdered(table: TableLayout) {
        val metadata = ListItemMetadata(EditorType.OL)
        table.tag = metadata
        for (i in 0 until table.childCount) {
            val childRow = table.getChildAt(i)
            val editText = childRow.findViewById<View>(R.id.txtText) as CustomEditText
            editText.tag = ListItemMetadata(EditorType.OL_LI)
            childRow.tag = ListItemMetadata(EditorType.OL_LI)
            val bullet = childRow.findViewById<View>(R.id.lblOrder) as TextView
            bullet.text = "${i+1}."
        }
    }

    fun getTextFromListItem(row: View): String {
        val _text = row.findViewById<View>(R.id.txtText) as CustomEditText
        return _text.text.toString()
    }

    fun insertlist(isOrdered: Boolean) {
        val activeView = editorCore.activeView
        val currentFocus = editorCore.getControlType(activeView)
        if (currentFocus === EditorType.UL_LI && !isOrdered) {
            /* this means, current focus is on n unordered list item, since user clicked
                 on unordered list icon, loop through the parents childs and convert each list item into normal edittext
                 *
                 */
            val _row = activeView!!.parent as TableRow
            val _table = _row.parent as TableLayout
            convertListToNormalText(_table, _table.indexOfChild(_row))
            /* this means, current focus is on n unordered list item, since user clicked
                 on unordered list icon, loop through the parents childs and convert each list item into normal edittext
                 *
                 */

        } else if (currentFocus === EditorType.UL_LI && isOrdered) {

            /*
             * user clicked on ordered list item. since it's an unordered list, you need to loop through each and convert each
             * item into an ordered list.
             * */
            val _row = activeView!!.parent as TableRow
            val _table = _row.parent as TableLayout
            convertListToOrdered(_table)
            /*
             * user clicked on ordered list item. since it's an unordered list, you need to loop through each and convert each
             * item into an ordered list.
             * */
        } else if (currentFocus === EditorType.OL_LI && isOrdered) {
            /*
             *
             * this means the item was an ordered list, you need to convert the item into a normal EditText
             *
             * */
            val _row = activeView!!.parent as TableRow
            val _table = _row.parent as TableLayout
            convertListToNormalText(_table, _table.indexOfChild(_row))
            /*
             *
             * this means the item was an ordered list, you need to convert the item into a normal EditText
             *
             * */
        } else if (currentFocus === EditorType.OL_LI && !isOrdered) {
            /*
             *
             * this means the item was an ordered list, you need to convert the item into an unordered list
             *
             * */

            val _row = activeView!!.parent as TableRow
            val _table = _row.parent as TableLayout
            convertListToUnordered(_table)
            /*
             *
             * this means the item was an ordered list, you need to convert the item into an unordered list
             *
             * */
        } else if (isOrdered) {
            /*
             *
             * it's a normal edit text, convert it into an ordered list. but first check index-1, if it's ordered, should follow the order no.
             * if it's unordered, convert all of em to ordered.
             *
             * */
            val index_of_activeView = editorCore.parentView!!.indexOfChild(editorCore.activeView)
            val Index = editorCore.determineIndex(EditorType.OL_LI)
            //check if the active view has content
            val view = editorCore.parentView!!.getChildAt(Index)
            if (view != null) {
                val type = editorCore.getControlType(view) //if then, get the type of that view, this behaviour is so, if that line has text,
                // it needs to be converted to list item
                if (type === EditorType.INPUT) {
                    val text = (view as CustomEditText).text.toString()  //get the text, if not null, replace it with list item
                    editorCore.parentView!!.removeView(view)

                    if (Index == 0) {
                        insertList(Index, isOrdered, text)
                    } else if (editorCore.getControlType(editorCore.parentView!!.getChildAt(index_of_activeView - 1)) === EditorType.OL) {
                        val _table = editorCore.parentView!!.getChildAt(index_of_activeView - 1) as TableLayout
                        addListItem(_table, isOrdered, text)
                    } else {
                        insertList(Index, isOrdered, text)
                    }
                } else {
                    insertList(Index, isOrdered, "")    //otherwise
                }
            } else {
                insertList(Index, isOrdered, "")
            }


        } else {
            /*
             *
             * it's a normal edit text, convert it into an un-ordered list
             *
             * */

            val Index = editorCore.determineIndex(EditorType.UL_LI)
            //check if the active view has content
            val view = editorCore.parentView!!.getChildAt(Index)
            if (view != null) {
                val type = editorCore.getControlType(view) //if then, get the type of that view, this behaviour is so, if that line has text,
                // it needs to be converted to list item
                if (type === EditorType.INPUT) {
                    val text = (view as EditText).text.toString()  //get the text, if not null, replace it with list item
                    editorCore.parentView!!.removeView(view)
                    insertList(Index, false, text)
                } else {
                    insertList(Index, false, "")    //otherwise
                }
            } else {
                insertList(Index, false, "")
            }
        }

    }

    private fun convertListToUnordered(table: TableLayout) {
        val metadata = ListItemMetadata(EditorType.UL)
        table.tag = metadata
        for (i in 0 until table.childCount) {
            val childRow = table.getChildAt(i)
            val editText = childRow.findViewById<View>(R.id.txtText) as CustomEditText
            editText.tag = ListItemMetadata(EditorType.UL_LI)
            childRow.tag = ListItemMetadata(EditorType.UL_LI)
            val _bullet = childRow.findViewById<View>(R.id.lblOrder) as TextView
            _bullet.text = "•"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun rearrangeColumns(_table: TableLayout) {
        for (i in 0 until _table.childCount) {
            val tableRow = _table.getChildAt(i) as TableRow
            val _bullet = tableRow.findViewById<View>(R.id.lblOrder) as TextView
            _bullet.text = "${i+1}."
        }
    }


    fun validateAndRemoveLisNode(view: View, contentType: ListItemMetadata) {
        // If the person was on an active ul|li, move him to the previous node
        val _row = view.parent as TableRow
        val _table = _row.parent as TableLayout
        val indexOnList = _table.indexOfChild(_row)
        _table.removeView(_row)
        if (indexOnList > 0) {
            /**
             * check if the index of the deleted row is <0, if so, move the focus to the previous li
             */
            val focusrow = _table.getChildAt(indexOnList - 1) as TableRow
            val text = focusrow.findViewById<View>(R.id.txtText) as EditText
            /**
             * Rearrange the nodes
             */
            if (contentType.type == EditorType.OL_LI) {
                rearrangeColumns(_table)
            }
            if (text.requestFocus()) {
                text.setSelection(text.text.length)
            }
        } else {
            /**
             * The removed row was first on the list. delete the list, and set the focus to previous element on the editor
             */
            editorCore.removeParent(_table)
        }
    }

    fun setFocusToList(view: View, position: Int) {
        val tableLayout = view as TableLayout
        val count = tableLayout.childCount
        if (tableLayout.childCount > 0) {
            val tableRow = tableLayout.getChildAt(if (position == POSITION_START) 0 else count - 1) as TableRow

            val editText = tableRow.findViewById<View>(R.id.txtText) as EditText
            if (editText.requestFocus()) {
                editText.setSelection(editText.text.length)
            }
        }
    }


    fun getIndexOnEditorByEditText(customEditText: CustomEditText): Int {
        val tableRow = customEditText.parent as TableRow
        val tableLayout = tableRow.parent as TableLayout
        return tableLayout.indexOfChild(tableRow)
    }

    fun setFocusToSpecific(customEditText: CustomEditText): CustomEditText? {
        val tableRow = customEditText.parent as TableRow
        val tableLayout = tableRow.parent as TableLayout
        val indexOnTable = tableLayout.indexOfChild(tableRow)
        if (indexOnTable == 0) {
            //what if index is 0, get the previous on edittext
        }
        val prevRow = tableLayout.getChildAt(indexOnTable - 1) as TableRow
        if (prevRow != null) {
            val editText = tableRow.findViewById<View>(R.id.txtText) as CustomEditText
            if (editText.requestFocus()) {
                editText.setSelection(editText.text?.length ?: 0)
            }
            return editText
        }
        return null
    }

    fun applyStyles(view: View, element: Element) {
        val textView: TextView
        if (editorCore.renderType === RenderType.EDITOR) {
            textView = view.findViewById(R.id.txtText)
        } else {
            textView = view.findViewById(R.id.lblText)
        }
        componentsWrapper!!.inputExtensions!!.applyStyles(textView, element)
    }


    fun onRenderfromEditorState(state: EditorContent, item: Node) {
        var layout: TableLayout? = null
        var listItemView: View? = null

        for (i in item.childs!!.indices) {
            if (i == 0) {
                layout = insertList(state.nodes!!.indexOf(item), item.type === EditorType.OL, item.childs!![0].content!![0])
            } else {
                listItemView = addListItem(layout!!, item.type === EditorType.OL, item.childs!![i].content!![0])
            }

            if (i == 0) {
                listItemView = layout
            }

            val tv: TextView

            if (editorCore.renderType === RenderType.RENDERER) {
                tv = listItemView!!.findViewById(R.id.lblText)
            } else {
                tv = listItemView!!.findViewById(R.id.txtText)
            }


            if (item.childs!![i].contentStyles != null) {
                for (style in item.childs!![i].contentStyles!!) {
                    tv.tag = ListItemMetadata(EditorType.UL_LI)
                    componentsWrapper!!.inputExtensions!!.updateTextStyle(style, tv)
                }
            }
            if (!TextUtils.isEmpty(item.childs!![i].textSettings!!.textColor)) {
                tv.setTextColor(Color.parseColor(item.childs!![i].textSettings!!.textColor))
            }
        }
    }

    fun RenderList(isOrdered: Boolean, element: Element) {
        if (element.children().size > 0) {
            var li = element.child(0)
            var text = componentsWrapper!!.htmlExtensions!!.getHtmlSpan(li)
            val layout = componentsWrapper!!.listItemExtensions!!.insertList(editorCore.parentChildCount, isOrdered, text)
            for (i in 1 until element.children().size) {
                li = element.child(i)
                text = componentsWrapper!!.htmlExtensions!!.getHtmlSpan(li)
                val view = componentsWrapper!!.listItemExtensions!!.addListItem(layout, isOrdered, text)
                componentsWrapper!!.listItemExtensions!!.applyStyles(view, li)
            }
        }
    }

    fun setLineSpacing(lineSpacing: Float) {
        this.lineSpacing = lineSpacing
    }

    companion object {
        val POSITION_START = 0
        val POSITION_END = 1
    }
}
