package com.sawelo.wordmemorizer.util.diff_util

import androidx.recyclerview.widget.DiffUtil
import com.sawelo.wordmemorizer.data.data_class.relation_ref.CategoryWithInfo

object CategoryWithInfoDiffUtilCallback: DiffUtil.ItemCallback<CategoryWithInfo>() {
    override fun areItemsTheSame(oldItem: CategoryWithInfo, newItem: CategoryWithInfo): Boolean {
        return oldItem.category.categoryId == newItem.category.categoryId
    }

    override fun areContentsTheSame(oldItem: CategoryWithInfo, newItem: CategoryWithInfo): Boolean {
        return oldItem == newItem
    }
}