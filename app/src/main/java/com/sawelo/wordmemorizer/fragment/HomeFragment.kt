package com.sawelo.wordmemorizer.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
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
class HomeFragment : Fragment() {
    @Inject
    lateinit var wordRepository: WordRepository

    private val viewModel: MainViewModel by activityViewModels()
    private var binding: FragmentHomeBinding? = null
    private var viewPagerAdapter: CategoryAdapter? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager2? = null

    private var currentCategoryList: List<Category>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabLayout = binding?.fragmentMainTabsLayout
        viewPager = binding?.fragmentMainViewPager
        viewPagerAdapter = CategoryAdapter(this)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllCategories().distinctUntilChangedBy {
                it.map { category ->
                    category.categoryName
                }
            }.collectLatest { categories ->
                currentCategoryList = categories
                viewPagerAdapter?.setCategoryList(categories)
                viewPager?.adapter = viewPagerAdapter
                if (tabLayout != null && viewPager != null && categories.isNotEmpty()) {
                    TabLayoutMediator(tabLayout!!, viewPager!!) { tab, position ->
                        tab.text = categories[position].categoryName
                    }.attach()
                }
                viewPager?.offscreenPageLimit = (categories.size).coerceIn(1, 6)
                changeCurrentTab()
            }
        }

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
        NotificationFloatingBubbleService.wrapBubbleService(requireContext())
    }

    override fun onStop() {
        super.onStop()
        NotificationFloatingBubbleService.unwrapBubbleService(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        viewPagerAdapter = null
        tabLayout = null
        viewPager = null
    }

    override fun onDestroy() {
        super.onDestroy()
        FloatingAddWordWindowReceiver.closeWindow(requireContext())
    }

    fun changeCurrentTab() {
        if (viewModel.currentCategory != null) {
            viewPager?.currentItem = currentCategoryList!!.indexOfFirst {
                it.categoryName == viewModel.currentCategory!!.categoryName
            }
        }
    }
}