package com.sawelo.wordmemorizer.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sawelo.wordmemorizer.data.Category
import com.sawelo.wordmemorizer.fragment.CategoryFragment

class CategoryAdapter(
    fragment: Fragment,
    private val categoryList: List<Category>
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = categoryList.size

    override fun createFragment(position: Int): Fragment {
        val categoryFragment = CategoryFragment()
        val arguments = Bundle()
        arguments.putParcelable(CATEGORY_ARGS, categoryList[position])
        categoryFragment.arguments = arguments
        return categoryFragment
    }

    override fun getItemId(position: Int): Long {
        return categoryList[position].id.toLong()
    }

    companion object {
        const val CATEGORY_ARGS = "CATEGORY_ARGS"
    }
}