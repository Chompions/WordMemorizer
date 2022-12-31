package com.sawelo.wordmemorizer.utils

import androidx.recyclerview.widget.DiffUtil
import com.sawelo.wordmemorizer.data.Category

object CategoryDiffUtilCallback: DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem == newItem
    }
}