package com.sawelo.wordmemorizer.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sawelo.wordmemorizer.data.Category
import com.sawelo.wordmemorizer.fragment.CategoryFragment

class CategoryAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private var categoryList: List<Category>? = null

    override fun getItemCount(): Int = categoryList?.size ?: 0

    override fun createFragment(position: Int): Fragment {
        val categoryFragment = CategoryFragment()
        val arguments = Bundle()
        arguments.putParcelable(CATEGORY_ARGS, categoryList?.get(position))
        categoryFragment.arguments = arguments
        return categoryFragment
    }

    override fun getItemId(position: Int): Long {
        return categoryList?.get(position)?.id?.toLong() ?: 0L
    }

    fun setCategoryList(list: List<Category>?) {
        categoryList = list
    }

    companion object {
        const val CATEGORY_ARGS = "CATEGORY_ARGS"
    }
}