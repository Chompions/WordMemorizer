package com.sawelo.wordmemorizer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.sawelo.wordmemorizer.adapter.CategoryAdapter
import com.sawelo.wordmemorizer.databinding.FragmentMainBinding
import com.sawelo.wordmemorizer.viewmodel.MainViewModel

class MainFragment : Fragment() {
    private var binding: FragmentMainBinding? = null
    private val viewModel: MainViewModel by activityViewModels()
    var viewPager: ViewPager2? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tabLayout = binding?.fragmentMainTabsLayout
        viewPager = binding?.fragmentMainViewPager

        if (tabLayout != null && viewPager != null) {
            viewModel.getAllCategories().observe(viewLifecycleOwner) { categoryList ->
                val adapter = CategoryAdapter(this, categoryList)
                viewPager!!.adapter = adapter

                TabLayoutMediator(tabLayout, viewPager!!) { tab, position ->
                    tab.text = categoryList[position].categoryName
                }.attach()
            }
        }

        binding?.fragmentMainFab?.setOnClickListener {
            AddWordDialogFragment().show(childFragmentManager, null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        viewPager = null
    }

    companion object {
        const val MAIN_FRAGMENT_TAG = "MAIN_FRAGMENT_TAG"
    }
}