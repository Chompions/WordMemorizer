package com.sawelo.wordmemorizer.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sawelo.wordmemorizer.fragment.CategoryFragment

class CategoryAdapter(
    fragment: Fragment,
    private val categoryList: List<String>
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = categoryList.size

    override fun createFragment(position: Int): Fragment {
        val categoryFragment = CategoryFragment()
        return categoryFragment
    }
}