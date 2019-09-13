package com.github.irshulx.dialogs.selectColor

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.irshulx.R
import com.github.irshulx.utilities.MaterialColor
import kotlinx.android.synthetic.main.dialog_select_color.view.*

/**
 * Dialog view for color selection
 */
class SelectColorDialogView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var textColor: MaterialColor
    get() = textColorGrid.selectedColor
    set(value) {
        textColorGrid.selectedColor = value
    }

    init {
        inflate(context, R.layout.dialog_select_color, this)
    }
}