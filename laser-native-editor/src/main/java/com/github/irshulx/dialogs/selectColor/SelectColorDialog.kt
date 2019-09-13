package com.github.irshulx.dialogs.selectColor

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.github.irshulx.R
import com.github.irshulx.utilities.MaterialColor

/**
 * Dialog for select color of text and its background
 * [resultCallback] text and background color
 */
class SelectColorDialog(
        private val context : Context,
        private val selectedTextColor: MaterialColor,
        private val title: String,
        private val okButtonText: String,
        private val cancelButtonText: String,
        private val resultCallback: (MaterialColor?) -> Unit) {

    //Select dialog's style - colorAccent for options and ? for buttons
    fun show() : AlertDialog =
        AlertDialog
            .Builder(context, R.style.App_Activity_Main_Dialog_Theme)
            .setTitle(title)
            .setCancelable(true)
            .also { dialog ->
                // Inflate view
                SelectColorDialogView(context)
                    .apply {
                        textColor = selectedTextColor

                        dialog.setView(this)
                    }
            }
            .setPositiveButton(okButtonText) { dialog, _ ->
                (dialog as AlertDialog)
                    .apply {
                        val dialogView = this.findViewById<View>(R.id.selectColorDialogRoot)!!.parent as SelectColorDialogView
                        resultCallback(dialogView.textColor)
                    }
            }
            .setNegativeButton(cancelButtonText) { _, _ -> resultCallback (null)}
            .setOnCancelListener { resultCallback(null) }
            .show()
}