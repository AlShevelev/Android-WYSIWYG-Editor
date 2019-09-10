package com.github.irshulx.wysiwyg.Utilities

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.View

import java.io.InputStream
import java.net.URL

/**
 * Created by mkallingal on 12/30/2015.
 */
class HtmlImageParser(internal var container: View, internal var c: Context) : Html.ImageGetter {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun getDrawable(source: String): Drawable? {
        try {
            val urlDrawable = HTMLImageDrawable()
            val asyncTask = ImageGetterAsyncTask(
                    urlDrawable)

            if (Build.VERSION.SDK_INT < 11) {
                asyncTask.execute(source)
            } else {
                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, source)
            }
            return urlDrawable
        } catch (e: Exception) {
            Log.e(LOG, e.message)
        }

        return null

    }

    inner class ImageGetterAsyncTask(internal var urlDrawable: HTMLImageDrawable?) : AsyncTask<String, Void, Drawable>() {

        override fun doInBackground(vararg params: String): Drawable? {
            val source = params[0]
            return fetchDrawable(source)
        }

        override fun onPostExecute(result: Drawable) {
            try {
                if (urlDrawable != null) {
                    urlDrawable!!.setBounds(0, 0, 0 + result.intrinsicWidth,
                            0 + result.intrinsicHeight)

                    urlDrawable!!.drawable = result
                    this@HtmlImageParser.container.invalidate()
                }

            } catch (e: Exception) {
                Log.e(LOG, e.message)
            }

        }


        fun fetchDrawable(urlString: String): Drawable? {
            try {
                //                InputStream is = fetch(urlString);
                val imageURL = URL(urlString)
                val inputStream = imageURL.openStream()
                val drawable = Drawable.createFromStream(inputStream, "src")
                drawable.setBounds(0, 0, 0 + drawable.intrinsicWidth,
                        0 + drawable.intrinsicHeight)
                return drawable
            } catch (e: Exception) {
                return null
            }

        }


    }

    companion object {
        val LOG = HtmlImageParser::class.java.name
    }
}
