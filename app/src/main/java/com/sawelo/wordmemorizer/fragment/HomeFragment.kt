package com.sawelo.wordmemorizer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.sawelo.wordmemorizer.adapter.CategoryAdapter
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.databinding.FragmentHomeBinding
import com.sawelo.wordmemorizer.receiver.FloatingAddWordWindowReceiver
import com.sawelo.wordmemorizer.receiver.FloatingAddWordWindowReceiver.Companion.registerReceiver
import com.sawelo.wordmemorizer.receiver.FloatingAddWordWindowReceiver.Companion.unregisterReceiver
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), ListUpdateCallback, OnTabSelectedListener {
    private val viewModel: MainViewModel by activityViewModels()
    private var binding: FragmentHomeBinding? = null
    private var asyncDiffer: AsyncListDiffer<Category>? = null
    private var viewPagerAdapter: CategoryAdapter? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager2? = null

    private var floatingAddWordWindowReceiver: FloatingAddWordWindowReceiver? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabLayout = binding?.fragmentMainTabsLayout
        viewPager = binding?.fragmentMainViewPager

        viewPagerAdapter = CategoryAdapter(this)
        viewPager?.adapter = viewPagerAdapter

        asyncDiffer = AsyncListDiffer(this, viewModel.asyncDifferConfig)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getAllCategories().collectLatest { categories ->
                    asyncDiffer?.submitList(categories)
                    viewPager?.offscreenPageLimit =
                        if (categories.size > 1) categories.size - 1 else 1
                }
            }
        }

        tabLayout?.addOnTabSelectedListener(this)
        viewPager?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout?.selectTab(tabLayout?.getTabAt(position))
            }
        })

        floatingAddWordWindowReceiver = FloatingAddWordWindowReceiver()
        floatingAddWordWindowReceiver?.registerReceiver(requireContext())

        binding?.fragmentMainFab?.setOnClickListener {
            FloatingAddWordWindowReceiver.openWindow(requireContext(), viewModel.currentCategory)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        asyncDiffer = null
        viewPagerAdapter = null
        tabLayout = null
        viewPager = null

        floatingAddWordWindowReceiver?.unregisterReceiver(requireContext())
        FloatingAddWordWindowReceiver.closeWindow(requireContext())
    }

    override fun onInserted(position: Int, count: Int) {
        val currentList = asyncDiffer?.currentList
        (position until position + count).forEach { perPosition ->
            currentList?.get(perPosition)?.let { category ->
                tabLayout?.createTab(category, perPosition)
            }
        }
        viewPagerAdapter?.setCategoryList(currentList)
        viewPagerAdapter?.notifyItemRangeInserted(position, count)
    }

    override fun onRemoved(position: Int, count: Int) {
        val currentList = asyncDiffer?.currentList
        (position until position + count).forEach { perPosition ->
            tabLayout?.removeTabAt(perPosition)
        }
        viewPagerAdapter?.setCategoryList(currentList)
        viewPagerAdapter?.notifyItemRangeRemoved(position, count)
        viewPagerAdapter?.notifyItemChanged(position)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        val currentList = asyncDiffer?.currentList
        tabLayout?.removeTabAt(fromPosition)
        currentList?.get(toPosition)?.let { category ->
            tabLayout?.createTab(category, toPosition)
        }
        viewPagerAdapter?.setCategoryList(currentList)
        viewPagerAdapter?.notifyItemMoved(fromPosition, toPosition)
    }
    override fun onChanged(position: Int, count: Int, payload: Any?) {
        val currentList = asyncDiffer?.currentList
        (position until position + count).forEach { perPosition ->
            tabLayout?.removeTabAt(perPosition)
            currentList?.get(perPosition)?.let { category ->
                tabLayout?.createTab(category, perPosition)
            }
        }
        viewPagerAdapter?.setCategoryList(currentList)
        viewPagerAdapter?.notifyItemRangeChanged(position, count)
    }

    private fun TabLayout.createTab(category: Category, position: Int) {
        newTab().let {
            it.text = category.categoryName
            tabLayout?.addTab(it, position)
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        viewPager?.currentItem = tab.position
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {}
    override fun onTabReselected(tab: TabLayout.Tab?) {}

    fun setCurrentTab(category: Category) {
        val currentCategory = asyncDiffer?.currentList
        if (currentCategory != null) {
            viewPager?.currentItem = currentCategory.indexOf(category)
        }
    }

    companion object {
        const val MAIN_FRAGMENT_TAG = "MAIN_FRAGMENT_TAG"
    }
}