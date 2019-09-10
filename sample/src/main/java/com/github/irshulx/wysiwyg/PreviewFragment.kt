package com.github.irshulx.wysiwyg

import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import com.github.irshulx.Editor
import com.github.irshulx.EditorListener
import com.github.irshulx.models.EditorContent

import java.util.HashMap

class PreviewFragment : Fragment() {

    private var mSerialized: String? = null

    private val mListener: OnFragmentInteractionListener? = null

    val headingTypeface: Map<Int, String>
        get() {
            val typefaceMap = HashMap<Int, String>()
            typefaceMap[Typeface.NORMAL] = "fonts/GreycliffCF-Bold.ttf"
            typefaceMap[Typeface.BOLD] = "fonts/GreycliffCF-Heavy.ttf"
            typefaceMap[Typeface.ITALIC] = "fonts/GreycliffCF-Heavy.ttf"
            typefaceMap[Typeface.BOLD_ITALIC] = "fonts/GreycliffCF-Bold.ttf"
            return typefaceMap
        }

    val contentface: Map<Int, String>
        get() {
            val typefaceMap = HashMap<Int, String>()
            typefaceMap[Typeface.NORMAL] = "fonts/Lato-Medium.ttf"
            typefaceMap[Typeface.BOLD] = "fonts/Lato-Bold.ttf"
            typefaceMap[Typeface.ITALIC] = "fonts/Lato-MediumItalic.ttf"
            typefaceMap[Typeface.BOLD_ITALIC] = "fonts/Lato-BoldItalic.ttf"
            return typefaceMap
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mSerialized = arguments!!.getString(SERIALIZED)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_preview, container, false)

        val renderer = view.findViewById<View>(R.id.renderer) as Editor
        val headingTypeface = headingTypeface
        val contentTypeface = contentface
        renderer.headingTypeface = headingTypeface
        renderer.contentTypeface = contentTypeface
        renderer.setDividerLayout(R.layout.tmpl_divider_layout)
        //renderer.setEditorImageLayout(R.layout.tmpl_image_view)
        renderer.setListItemLayout(R.layout.tmpl_list_item)
        val content = mSerialized
        val Deserialized = renderer.getContentDeserialized(content!!)
        renderer.editorListener = object : EditorListener {
            override fun onTextChanged(editText: EditText, text: Editable) {

            }

            override fun onUpload(image: Bitmap, uuid: String) {

            }

            override fun onRenderMacro(name: String, settings: Map<String, Any>, index: Int): View {
                return layoutInflater.inflate(R.layout.layout_authored_by, null)
            }
        }
        renderer.render(Deserialized)
        return view
    }

    fun onButtonPressed(uri: Uri) {
        mListener?.onFragmentInteraction(uri)
    }

    interface OnFragmentInteractionListener {
        // TODO: UPDATE argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        private val SERIALIZED = ""
        fun newInstance(serialized: String): PreviewFragment {
            val fragment = PreviewFragment()
            val args = Bundle()
            args.putString(SERIALIZED, serialized)
            fragment.arguments = args
            return fragment
        }
    }
}
