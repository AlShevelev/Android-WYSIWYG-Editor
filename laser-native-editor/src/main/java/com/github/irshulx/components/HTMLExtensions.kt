package com.github.irshulx.components

import com.github.irshulx.EditorCore
import com.github.irshulx.models.EditorType
import com.github.irshulx.models.HtmlTag

import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import java.lang.UnsupportedOperationException

import java.util.HashMap

class HTMLExtensions(internal var editorCore: EditorCore) {
    companion object {
        fun matchesTag(test: String): Boolean {
            for (tag in HtmlTag.values()) {
                if (tag.name == test) {
                    return true
                }
            }
            return false
        }
    }

    fun getStyleMap(element: Element): Map<String, String> {
        val keymaps = HashMap<String, String>()
        if (!element.hasAttr("style")) {
            return keymaps
        }
        val styleStr = element.attr("style") // => margin-top:10px;color:#fcc;border-bottom:1px solid #ccc; background-color: #333; text-align:center
        val keys = styleStr.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var split: Array<String>

        if (keys.size > 1) {
            for (i in keys.indices) {
                if (i % 2 != 0) {
                    split = keys[i].split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (split.size == 1) break
                    keymaps[split[1].trim { it <= ' ' }] = keys[i + 1].split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' }
                } else {
                    split = keys[i].split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (i + 1 == keys.size) break
                    keymaps[keys[i].split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[split.size - 1].trim { it <= ' ' }] = keys[i + 1].split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' }
                }
            }
        }
        return keymaps
    }

    fun getHtmlSpan(element: Element): String {
        val el = Element(Tag.valueOf("spanInfo"), "")
        el.attributes().put("style", element.attr("style"))
        el.html(element.html())
        return el.toString()
    }

    private fun hasChildren(element: Element): Boolean {
        return element.allElements.size > 0
    }

    fun getTemplateHtml(child: EditorType): String =
        when (child) {
            EditorType.INPUT -> "<{{\$tag}} data-tag=\"input\" {{\$style}}>{{\$content}}</{{\$tag}}>"
            EditorType.HR -> "<hr data-tag=\"hr\"/>"
            EditorType.IMG -> "<div data-tag=\"img\"><img src=\"{{\$url}}\" />{{\$img-sub}}</div>"
            EditorType.IMG_SUB -> "<{{\$tag}} data-tag=\"img-sub\" {{\$style}} class=\"editor-image-subtitle\">{{\$content}}</{{\$tag}}>"
            EditorType.MAP -> "<div data-tag=\"map\"><img src=\"{{\$content}}\" /><span text-align:'center' {{\$style}}>{{\$desc}}</span></div>"
            EditorType.OL -> "<ol data-tag=\"ol\">{{\$content}}</ol>"
            EditorType.UL -> "<ul data-tag=\"ul\">{{\$content}}</ul>"
            EditorType.OL_LI, EditorType.UL_LI -> {
                val dataTag = if (child === EditorType.OL_LI) "data-tag=\"list-item-ol\"" else "data-tag=\"list-item-ul\""
                "<li $dataTag><{{\$tag}} {{\$style}}>{{\$content}}</{{\$tag}}></li>"
            }
            else -> throw UnsupportedOperationException("This editor typs is not supported: $child")
        }
}