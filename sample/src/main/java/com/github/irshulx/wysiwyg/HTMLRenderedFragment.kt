package com.github.irshulx.wysiwyg


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

import com.github.irshulx.Editor


/**
 * A simple [Fragment] subclass.
 * Use the [HTMLRenderedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HTMLRenderedFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var mSerialized: String? = null
    private var mSerializedHtml: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mSerialized = arguments!!.getString(SERIALIZED)
            val editor = Editor(context!!, null!!)
            mSerializedHtml = editor.getContentAsHTML(mSerialized!!)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_htmlrendered, container, false)
        (view.findViewById<View>(R.id.lblHtmlRendered) as TextView).text = mSerializedHtml
        return view
    }

    companion object {
        private val SERIALIZED = ""
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param serialized Parameter 1.
         * @return A new instance of fragment PreviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(serialized: String): HTMLRenderedFragment {
            val fragment = HTMLRenderedFragment()
            val args = Bundle()
            args.putString(SERIALIZED, serialized)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
