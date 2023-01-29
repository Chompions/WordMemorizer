package com.sawelo.wordmemorizer.util

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.util.WordUtils.isAll

object ViewUtils {

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
}