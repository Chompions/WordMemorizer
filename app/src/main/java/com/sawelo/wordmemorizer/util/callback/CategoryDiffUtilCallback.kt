package com.sawelo.wordmemorizer.util.callback

import androidx.recyclerview.widget.DiffUtil
import com.sawelo.wordmemorizer.data.data_class.Category

object CategoryDiffUtilCallback: DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.categoryId == newItem.categoryId
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem == newItem
    }
}