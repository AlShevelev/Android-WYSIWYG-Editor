package com.github.irshulx.wysiwyg

import android.app.Application


/**
 * Created by mkallingal on 12/23/2015.
 */
class MyApplication : Application() {

    var someVariable: String? = null

    override fun onCreate() {
        super.onCreate()
    }
}