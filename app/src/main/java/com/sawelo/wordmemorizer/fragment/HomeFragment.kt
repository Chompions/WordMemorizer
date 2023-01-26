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
    private var adapter: CategoryAdapter? = null
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

        adapter = CategoryAdapter(this)
        viewPager?.adapter = adapter

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
            FloatingAddWordWindowReceiver.openWindow(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        asyncDiffer = null
        adapter = null
        tabLayout = null
        viewPager = null

        floatingAddWordWindowReceiver?.unregisterReceiver(requireContext())
        FloatingAddWordWindowReceiver.closeWindow(requireContext())
    }

    override fun onInserted(position: Int, count: Int) {
        adapter?.setCategoryList(asyncDiffer?.currentList)
        (position until position + count).forEach { perPosition ->
            asyncDiffer?.currentList?.get(perPosition)?.let { category ->
                val newTab = tabLayout?.newTab()?.setText(category.categoryName)
                if (newTab != null) {
                    tabLayout?.addTab(newTab)
                    adapter?.notifyItemInserted(perPosition)
                }
            }
        }
    }

    override fun onRemoved(position: Int, count: Int) {
        adapter?.setCategoryList(asyncDiffer?.currentList)
        (position until position + count).forEach { perPosition ->
            tabLayout?.removeTabAt(perPosition)
            adapter?.notifyItemRemoved(perPosition)
        }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}

    override fun onTabSelected(tab: TabLayout.Tab) {
        viewPager?.currentItem = tab.position
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {}
    override fun onTabReselected(tab: TabLayout.Tab?) {}

    fun setCurrentTab(index: Int) {
        viewPager?.currentItem = index
    }

    companion object {
        const val MAIN_FRAGMENT_TAG = "MAIN_FRAGMENT_TAG"
    }
}