package com.github.irshulx.wysiwyg.Utilities

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.github.irshulx.wysiwyg.HTMLRenderedFragment
import com.github.irshulx.wysiwyg.PreviewFragment
import com.github.irshulx.wysiwyg.SerializedFragment

/**
 * Created by mkallingal on 6/12/2016.
 */
class RendererPagerAdapter(fm: FragmentManager, private val context: Context, internal val content: String) : FragmentPagerAdapter(fm) {
    internal val PAGE_COUNT = 3
    private val tabTitles = arrayOf("Rendered", "Serialized", "HTML")

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            PreviewFragment.newInstance(content)
        } else if (position == 1) {
            SerializedFragment.newInstance(content)
        } else {
            HTMLRenderedFragment.newInstance(content)
        }
    }

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getPageTitle(position: Int): CharSequence? {
        // Generate title based on item position
        return tabTitles[position]
    }

}
