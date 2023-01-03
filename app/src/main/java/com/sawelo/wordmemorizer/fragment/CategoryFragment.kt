package com.sawelo.wordmemorizer.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sawelo.wordmemorizer.adapter.CategoryAdapter
import com.sawelo.wordmemorizer.adapter.MainWordAdapter
import com.sawelo.wordmemorizer.adapter.SimilarWordAdapter
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.databinding.FragmentCategoryBinding
import com.sawelo.wordmemorizer.util.callback.ItemWordAdapterCallback
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CategoryFragment : Fragment(), ItemWordAdapterCallback {
    private val viewModel: MainViewModel by activityViewModels()
    private var binding: FragmentCategoryBinding? = null
    private var mainWordAdapter: MainWordAdapter? = null
    private var mainWordRv: RecyclerView? = null
    private var similarWordAdapter: SimilarWordAdapter? = null
    private var similarWordRv: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onItemHideBtnClickListener(word: Word) {
        viewModel.updateIsForgottenWord(word, false)
    }

    override fun onItemForgotBtnClickListener(word: Word) {
        viewModel.updateIsForgottenWord(word, true)
        viewModel.updateForgotCountWord(word)
    }

    override fun onItemLongClickListener(word: Word) {
        viewModel.deleteWord(word)
        showToast("You deleted ${word.wordText}")
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

        // Get arguments
        @Suppress("DEPRECATION") val parcelable = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable(CategoryAdapter.CATEGORY_ARGS, Category::class.java)
        } else {
            arguments?.getParcelable(CategoryAdapter.CATEGORY_ARGS)
        }

        parcelable?.also { category ->
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.getAllWordsPagingData(category)
                        .onEach {
                            binding?.fragmentCategoryMainWordsProgressIndicator?.show()
                        }.collectLatest {
                            mainWordAdapter?.submitData(it)
                        }
                }
            }

            // Collect all forgotten words
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

    override fun onResume() {
        super.onResume()
        viewModel.currentCategoryFragmentTag = tag
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        mainWordAdapter = null
        mainWordRv = null
        similarWordAdapter = null
        similarWordRv = null
    }

    private fun showToast(text: String) {
        Toast
            .makeText(requireContext(), text, Toast.LENGTH_SHORT)
            .show()
    }
}