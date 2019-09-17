package com.github.irshulx.wysiwyg

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment

import com.github.irshulx.Editor
import com.github.irshulx.EditorListener

class PreviewFragment : Fragment() {

    private var mSerialized: String? = null

    private val mListener: OnFragmentInteractionListener? = null

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
        renderer.setDividerLayout(R.layout.widget_divider)
        //renderer.setEditorImageLayout(R.layout.tmpl_image_view)
        val content = mSerialized
        val Deserialized = renderer.getContentDeserialized(content!!)
        renderer.editorListener = object : EditorListener {
            override fun onTextChanged(editText: EditText, text: Editable) {

            }

            override fun onUpload(image: Bitmap, uuid: String) {

            }

            override fun onRenderMacro(name: String, settings: Map<String, Any>, index: Int): View {
                return View(this@PreviewFragment.context)
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
