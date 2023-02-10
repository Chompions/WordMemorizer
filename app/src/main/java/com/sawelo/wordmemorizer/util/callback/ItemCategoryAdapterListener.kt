package com.sawelo.wordmemorizer.util.callback

import com.sawelo.wordmemorizer.data.data_class.entity.Category

interface ItemCategoryAdapterListener {
    fun onCheckedChangedListener(isSelected: Boolean, item: Category)
    fun onLongClickListener(item: Category)
}