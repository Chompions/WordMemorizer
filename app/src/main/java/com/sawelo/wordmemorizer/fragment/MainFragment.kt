package com.sawelo.wordmemorizer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.sawelo.wordmemorizer.adapter.CategoryAdapter
import com.sawelo.wordmemorizer.databinding.FragmentMainBinding
import com.sawelo.wordmemorizer.viewmodel.MainViewModel

class MainFragment : Fragment() {
    private var binding: FragmentMainBinding? = null
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val categoryList = listOf("All", "Names", "Verbs")

        val tabLayout = binding?.activityMainTabsLayout
        val viewPager = binding?.activityMainViewPager

        if (tabLayout != null && viewPager != null) {
            val adapter = CategoryAdapter(this, categoryList)
            viewPager.adapter = adapter

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = categoryList[position]
            }.attach()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}