package com.github.irshulx.wysiwyg

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.github.irshulx.wysiwyg.Utilities.RendererPagerAdapter
import kotlinx.android.synthetic.main.activity_render_test.*

class RenderTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_render_test)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val serialized = intent.getStringExtra("content")
        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        viewPager.adapter = RendererPagerAdapter(supportFragmentManager,
                this@RenderTestActivity, serialized)

        // Give the TabLayout the ViewPager
        val tabLayout = sliding_tabs
        tabLayout.setupWithViewPager(viewPager)
    }
}
