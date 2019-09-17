package com.github.irshulx.wysiwyg

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.irshulx.Editor
import com.github.irshulx.EditorCore
import com.github.irshulx.EditorListener
import com.github.irshulx.dialogs.selectColor.SelectColorDialog
import com.github.irshulx.models.EditorTextStyle
import com.github.irshulx.utilities.MaterialColor
import kotlinx.android.synthetic.main.activity_editor_test.*
import java.io.IOException

class EditorTestActivity : AppCompatActivity() {
    internal lateinit var editor: Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_test)
        editor = editorWidget
        setUpEditor()
    }

    private fun setUpEditor() {
        findViewById<View>(R.id.action_bold).setOnClickListener { editor.updateTextStyle(EditorTextStyle.BOLD) }

        findViewById<View>(R.id.action_Italic).setOnClickListener { editor.updateTextStyle(EditorTextStyle.ITALIC) }

        findViewById<View>(R.id.action_hr).setOnClickListener { editor.insertDivider() }


        findViewById<View>(R.id.action_color).setOnClickListener {
            SelectColorDialog(this, MaterialColor.BLACK, "Select color", "Ok", "Cancel") { selectedColor ->
                selectedColor?.let { editor.updateTextColor(it) }
            }
            .show()
        }

        findViewById<View>(R.id.action_insert_image).setOnClickListener { editor.openImagePicker() }

        findViewById<View>(R.id.action_insert_link).setOnClickListener { editor.insertLink() }

        findViewById<View>(R.id.action_erase).setOnClickListener { editor.clearAllContents() }

        findViewById<View>(R.id.action_tag).setOnClickListener { editor.insertTag("mutin_pudak") }
        findViewById<View>(R.id.action_mention).setOnClickListener { editor.insertMention("barklay") }
        findViewById<View>(R.id.action_link_in_text).setOnClickListener { editor.insertLinkInText("Google", "https://www.google.com") }

        findViewById<View>(R.id.action_tag_edit).setOnClickListener { editor.editTag("blah_blah") }
        findViewById<View>(R.id.action_mention_edit).setOnClickListener { editor.editMention("suvorov") }
        findViewById<View>(R.id.action_link_in_text_edit).setOnClickListener { editor.editLinkInText("Yandex", "https://yandex.ru") }

        editor.setOnSelectionTextChangeListener { isSelected ->
            Log.d("SELECTION_TEST", "Area is selected: $isSelected")
        }

        editor.setDividerLayout(R.layout.widget_divider)
        editor.editorListener = object : EditorListener {
            override fun onTextChanged(editText: EditText, text: Editable) {
                // Toast.makeText(EditorTestActivity.this, text, Toast.LENGTH_SHORT).show();
            }

            override fun onUpload(image: Bitmap, uuid: String) {
                Toast.makeText(this@EditorTestActivity, uuid, Toast.LENGTH_LONG).show()
                /**
                 * TODO do your upload here from the bitmap received and all onImageUploadComplete(String url); to insert the result url to
                 * let the editor know the upload has completed
                 */
                editor.onImageUploadComplete("http://www.videogamesblogger.com/wp-content/uploads/2015/08/metal-gear-solid-5-the-phantom-pain-cheats-640x325.jpg", uuid)
                // editor.onImageUploadFailed(uuid);
            }

            override fun onRenderMacro(name: String, props: Map<String, Any>, index: Int): View {
                return View(this@EditorTestActivity)
            }

        }

        findViewById<View>(R.id.btnRender).setOnClickListener {
            val text = editor.contentAsSerialized
            editor.contentAsHTML

            val intent = Intent(applicationContext, RenderTestActivity::class.java)
            intent.putExtra("content", text)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EditorCore.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val uri = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                // Log.d(TAG, String.valueOf(bitmap));
                editor.insertImage(bitmap)
            } catch (e: IOException) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }

        } else if (resultCode == Activity.RESULT_CANCELED) {
            //Write your code if there's no result
            Toast.makeText(applicationContext, "Cancelled", Toast.LENGTH_SHORT).show()
            // editor.RestoreState();
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
                .setTitle("Exit Editor?")
                .setMessage("Are you sure you want to exit the editor?")
                .setPositiveButton("Yes") { dialog, which -> finish() }
                .setNegativeButton("No", null)
                .show()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setGhost(findViewById<View>(R.id.btnRender) as Button)
    }

    companion object {

        fun setGhost(button: Button) {
            val radius = 4
            val background = GradientDrawable()
            background.shape = GradientDrawable.RECTANGLE
            background.setStroke(4, Color.WHITE)
            background.cornerRadius = radius.toFloat()
            button.setBackgroundDrawable(background)
        }
    }
}