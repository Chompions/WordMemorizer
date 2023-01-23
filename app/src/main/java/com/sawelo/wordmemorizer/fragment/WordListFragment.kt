package com.sawelo.wordmemorizer.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sawelo.wordmemorizer.activity.EditWordActivity
import com.sawelo.wordmemorizer.adapter.MainWordAdapter
import com.sawelo.wordmemorizer.adapter.SimilarWordAdapter
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.databinding.FragmentWordListBinding
import com.sawelo.wordmemorizer.util.callback.ItemWordAdapterCallback
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class WordListFragment : Fragment(), ItemWordAdapterCallback {
    private val viewModel: MainViewModel by activityViewModels()
    private var binding: FragmentWordListBinding? = null
    private var mainWordAdapter: MainWordAdapter? = null
    private var mainWordRv: RecyclerView? = null
    private var similarWordAdapter: SimilarWordAdapter? = null
    private var similarWordRv: RecyclerView? = null

    private var currentCategory: Category? = null
    private var currentCategoryList: List<Category>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWordListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onItemHideBtnClickListener(item: Word) {
        viewModel.updateHideForgotWord(item)
    }

    override fun onItemForgotBtnClickListener(item: Word) {
        viewModel.updateShowForgotWord(item)
    }

    override fun onItemLongClickListener(item: Word) {
        lifecycleScope.launch {
            EditWordActivity.startActivity(
                activity, item.wordId, viewModel.getAllCategories().first())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Setting adapters
        mainWordAdapter = MainWordAdapter(this).apply {
            addOnPagesUpdatedListener {
                binding?.fragmentCategoryMainWordsProgressIndicator?.hide()
            }
        }
        similarWordAdapter = SimilarWordAdapter(this).apply {
            addOnPagesUpdatedListener {
                binding?.fragmentCategorySimilarWordsProgressIndicator?.hide()
            }
        }

        // Setting recycler views
        mainWordRv = binding?.fragmentCategoryMainWordsRv?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mainWordAdapter
        }
        similarWordRv = binding?.fragmentCategorySimilarWordsRv?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = similarWordAdapter
        }

        // Get parcelable
        getParcelable()

        // Set list depending on currentCategory
        currentCategory?.also { category ->
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.getAllWordsPagingData(category)
                        .onEach {
                            binding?.fragmentCategoryMainWordsProgressIndicator?.show()
                        }
                        .collectLatest {
                            mainWordAdapter?.submitData(it)
                        }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.getAllForgottenWordsPagingData(category)
                        .onEach {
                            binding?.fragmentCategorySimilarWordsProgressIndicator?.show()
                        }
                        .collectLatest {
                            similarWordAdapter?.submitData(it)
                        }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        mainWordAdapter = null
        mainWordRv = null
        similarWordAdapter = null
        similarWordRv = null

        currentCategory = null
        currentCategoryList = null
    }

    @Suppress("DEPRECATION")
    private fun getParcelable() {
        // Get argument for currentCategory
        currentCategory = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable(WORD_LIST_FRAGMENT_CATEGORY_ARGS, Category::class.java)
        } else {
            arguments?.getParcelable(WORD_LIST_FRAGMENT_CATEGORY_ARGS)
        }

        // Get argument for currentCategory
        currentCategoryList = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelableArrayList(WORD_LIST_FRAGMENT_CATEGORY_LIST_ARGS, Category::class.java)
        } else {
            arguments?.getParcelableArrayList(WORD_LIST_FRAGMENT_CATEGORY_LIST_ARGS)
        }
    }

    companion object {
        fun newInstance(
            currentCategory: Category,
            categoryList: List<Category>
        ): WordListFragment {
            val dialogFragment = WordListFragment()
            dialogFragment.arguments = Bundle().apply {
                putParcelable(WORD_LIST_FRAGMENT_CATEGORY_ARGS, currentCategory)
                putParcelableArrayList (
                    WORD_LIST_FRAGMENT_CATEGORY_LIST_ARGS,
                    ArrayList(categoryList)
                )
            }
            return dialogFragment
        }

        const val WORD_LIST_FRAGMENT_CATEGORY_ARGS = "WORD_LIST_FRAGMENT_CATEGORY_ARGS"
        const val WORD_LIST_FRAGMENT_CATEGORY_LIST_ARGS = "WORD_LIST_FRAGMENT_CATEGORY_LIST_ARGS"
    }
}