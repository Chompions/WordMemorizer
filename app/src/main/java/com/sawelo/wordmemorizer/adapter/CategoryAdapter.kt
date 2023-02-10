package com.sawelo.wordmemorizer.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import com.sawelo.wordmemorizer.data.data_class.relation_ref.CategoryWithInfo
import com.sawelo.wordmemorizer.util.callback.ItemCategoryAdapterListener
import com.sawelo.wordmemorizer.util.diff_util.CategoryWithInfoDiffUtilCallback

class CategoryAdapter(
    private val callback: ItemCategoryAdapterListener
): ListAdapter<CategoryWithInfo, CategoryAdapter.CategoryViewHolder>(CategoryWithInfoDiffUtilCallback) {

    private var selectedCategory = emptyList<Category>()

    inner class CategoryViewHolder(itemView: View) : ViewHolder(itemView) {
        val categoryCheckBox: CheckBox = itemView.findViewById(R.id.itemDrawer_checkBox)
        val categoryNameTv: TextView = itemView.findViewById(R.id.itemDrawer_name_tv)
        val categoryDescTv: TextView = itemView.findViewById(R.id.itemDrawer_desc_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drawer_category, parent, false)
        return CategoryViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryItem = getItem(position)

        holder.categoryNameTv.text =
            "${categoryItem.category.categoryName} (${categoryItem.categoryInfo.wordCount})"

        holder.categoryDescTv.isVisible =
            if (categoryItem.category.categoryDesc.isNullOrBlank()) false
            else {
                holder.categoryDescTv.text = categoryItem.category.categoryDesc
                true
            }

        holder.categoryCheckBox.apply {
            text = categoryItem.category.categoryName
            isChecked = categoryItem.category in selectedCategory
            setOnCheckedChangeListener { _, b ->
                callback.onCheckedChangedListener(b, categoryItem.category)
            }
        }

        holder.itemView.setOnLongClickListener {
            callback.onLongClickListener(categoryItem.category)
            true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedCategory(list: List<Category>) {
        selectedCategory = list
        notifyDataSetChanged()
    }
}