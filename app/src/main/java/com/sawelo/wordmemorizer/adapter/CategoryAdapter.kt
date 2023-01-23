package com.sawelo.wordmemorizer.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.fragment.WordListFragment

class CategoryAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private var categoryList: List<Category>? = null

    override fun getItemCount(): Int = categoryList?.size ?: 0

    override fun createFragment(position: Int): Fragment {
        if (categoryList != null) {
            return WordListFragment.newInstance(categoryList!![position], categoryList!!)
        } else throw Exception("Category list must not be null")
    }

    fun setCategoryList(list: List<Category>?) {
        categoryList = list
    }
}