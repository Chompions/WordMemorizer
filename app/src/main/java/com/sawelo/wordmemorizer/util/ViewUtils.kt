package com.sawelo.wordmemorizer.util

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.google.android.material.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputLayout
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.util.WordUtils.isAll
import com.sawelo.wordmemorizer.window.ToastWindow

object ViewUtils {

    @ColorInt
    fun Context.getColorFromAttr(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    fun Context.showToast(toast: String?) {
        if (Settings.canDrawOverlays(this)) {
            toast?.let { ToastWindow(this, it) }
        } else {
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show()
        }
    }

    fun LinearLayout.addButtonInLayout(
        context: Context,
        inputText: String,
        defStyleAttr: Int = R.attr.materialIconButtonFilledTonalStyle,
        onClick: () -> Unit
    ) {
        val recommendationButton = MaterialButton(
            context, null,
            defStyleAttr
        ).apply {
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 4, 0)
            layoutParams = params
            text = inputText
            setOnClickListener {
                onClick.invoke()
            }
        }
        this.addView(recommendationButton)
    }

    fun MaterialButtonToggleGroup.addCategoryList(
        context: Context,
        categoryList: List<Category>
    ) {
        for (category in categoryList) {
            if (!category.isAll()) {
                val button = MaterialButton(
                    context, null,
                    R.attr.materialButtonOutlinedStyle
                ).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    text = category.categoryName
                    id = category.categoryId
                }
                this.addView(button)
            }
        }
    }

    fun TextInputLayout.checkCopyOrPaste(isFocused: Boolean) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        when {
            editText?.text.isNullOrBlank() && isFocused && clipboardManager.primaryClipDescription
                ?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true -> {
                isEndIconVisible = true
                setEndIconDrawable(com.sawelo.wordmemorizer.R.drawable.baseline_content_paste_24)
                setEndIconOnClickListener {
                    val clipData = clipboardManager.primaryClip
                    val pastedText = clipData?.getItemAt(0)?.coerceToText(context)
                    this.editText?.setText(pastedText)
                    context.showToast("Text pasted")
                }
            }
            !editText?.text.isNullOrBlank() && isFocused -> {
                isEndIconVisible = true
                setEndIconDrawable(com.sawelo.wordmemorizer.R.drawable.baseline_content_copy_24)
                setEndIconOnClickListener {
                    val clipData = ClipData.newPlainText("word", this.editText?.text)
                    clipboardManager.setPrimaryClip(clipData)
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                        context.showToast("Text copied")
                }
            }
            else -> isEndIconVisible = false
        }
    }

}