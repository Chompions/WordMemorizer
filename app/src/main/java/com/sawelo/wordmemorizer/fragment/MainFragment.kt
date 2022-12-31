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
import com.sawelo.wordmemorizer.data.Category
import com.sawelo.wordmemorizer.databinding.FragmentMainBinding
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainFragment : Fragment(), ListUpdateCallback, OnTabSelectedListener {
    private val viewModel: MainViewModel by activityViewModels()
    private var binding: FragmentMainBinding? = null
    private var asyncDiffer: AsyncListDiffer<Category>? = null
    private var adapter: CategoryAdapter? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager2? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
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
                    viewPager?.offscreenPageLimit = categories.size - 1
                }
            }
        }

        tabLayout?.addOnTabSelectedListener(this)
        viewPager?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout?.selectTab(tabLayout?.getTabAt(position))
            }
        })

        binding?.fragmentMainFab?.setOnClickListener {
            if (asyncDiffer != null) {
                AddWordDialogFragment
                    .newInstance(asyncDiffer!!.currentList)
                    .show(childFragmentManager, null)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        asyncDiffer = null
        adapter = null
        tabLayout = null
        viewPager = null
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