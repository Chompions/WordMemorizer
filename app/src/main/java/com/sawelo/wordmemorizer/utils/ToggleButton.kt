package com.sawelo.wordmemorizer.utils

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton

class MaterialToggleButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.style.Widget_Material3_Button_OutlinedButton,
) : MaterialButton(context, attributeSet, defStyleAttr)