package com.sawelo.wordmemorizer.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.fragment.WordListFragment

class CategoryAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private var categoryList: List<Category> = emptyList()
    override fun getItemCount(): Int = categoryList.size

    override fun createFragment(position: Int): Fragment {
        return WordListFragment.newInstance(categoryList[position], categoryList)
    }

    fun setCategoryList(list: List<Category>) {
        categoryList = list
    }
}