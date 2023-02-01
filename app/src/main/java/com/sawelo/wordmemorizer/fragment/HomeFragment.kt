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
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.databinding.FragmentHomeBinding
import com.sawelo.wordmemorizer.receiver.FloatingAddWordWindowReceiver
import com.sawelo.wordmemorizer.service.NotificationFloatingBubbleService
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import com.sawelo.wordmemorizer.window.dialog.FloatingAddWordWindowInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), ListUpdateCallback {
    @Inject
    lateinit var wordRepository: WordRepository

    private val viewModel: MainViewModel by activityViewModels()
    private var binding: FragmentHomeBinding? = null
    private var asyncDiffer: AsyncListDiffer<Category>? = null
    private var viewPagerAdapter: CategoryAdapter? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager2? = null

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
                viewModel.getAllCategories().distinctUntilChangedBy {
                    it.map { category ->
                        category.categoryId
                    }
                }.collectLatest { categories ->
                    asyncDiffer?.submitList(categories)
                    viewPager?.offscreenPageLimit =
                        if (categories.size > 1) categories.size - 1 else 1
                }
            }
        }

        tabLayout?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager?.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                viewPager?.currentItem = tab.position
            }
        })
        viewPager?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout?.selectTab(tabLayout?.getTabAt(position))
            }
        })

        binding?.fragmentMainFab?.setOnClickListener {
            FloatingAddWordWindowInstance(
                requireActivity(), wordRepository, viewModel.currentCategory
            ).also { instance ->
                instance.showInstance()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        NotificationFloatingBubbleService.hideBubbleService(requireContext())
    }

    override fun onStop() {
        super.onStop()
        NotificationFloatingBubbleService.revealBubbleService(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        asyncDiffer = null
        viewPagerAdapter = null
        tabLayout = null
        viewPager = null
    }

    override fun onDestroy() {
        super.onDestroy()
        FloatingAddWordWindowReceiver.closeWindow(requireContext())
    }

    override fun onInserted(position: Int, count: Int) {
        val currentList = asyncDiffer?.currentList
        (position until position + count).forEach { perPosition ->
            currentList?.get(perPosition)?.let { category ->
                tabLayout?.createTab(category, perPosition)
            }
        }
        if (currentList != null) {
            viewPagerAdapter?.setCategoryList(currentList)
            viewPagerAdapter?.notifyItemRangeInserted(position, count)
        }
    }

    override fun onRemoved(position: Int, count: Int) {
        val currentList = asyncDiffer?.currentList
        (position until position + count).forEach { perPosition ->
            tabLayout?.removeTabAt(perPosition)
        }
        if (currentList != null) {
            viewPagerAdapter?.setCategoryList(currentList)
            viewPagerAdapter?.notifyItemRangeRemoved(position, count)
            viewPagerAdapter?.notifyItemRangeChanged(position, count)
        }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        val currentList = asyncDiffer?.currentList
        tabLayout?.removeTabAt(fromPosition)
        currentList?.get(toPosition)?.let { category ->
            tabLayout?.createTab(category, toPosition)
        }
        if (currentList != null) {
            viewPagerAdapter?.setCategoryList(currentList)
            viewPagerAdapter?.notifyItemMoved(fromPosition, toPosition)
        }
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        val currentList = asyncDiffer?.currentList
        (position until position + count).forEach { perPosition ->
            val perPositionTab = tabLayout?.getTabAt(perPosition)
            val perPositionCategory = currentList?.get(perPosition)
            perPositionTab?.text = perPositionCategory?.categoryName
        }
        if (currentList != null) {
            viewPagerAdapter?.setCategoryList(currentList)
            viewPagerAdapter?.notifyItemRangeChanged(position, count)
        }
    }

    private fun TabLayout.createTab(category: Category, position: Int) {
        newTab().let {
            it.text = category.categoryName
            tabLayout?.addTab(it, position)
        }
    }

    fun setCurrentTab(category: Category) {
        val currentCategory = asyncDiffer?.currentList
        if (currentCategory != null) {
            viewPager?.currentItem = currentCategory.indexOf(category)
        }
    }
}