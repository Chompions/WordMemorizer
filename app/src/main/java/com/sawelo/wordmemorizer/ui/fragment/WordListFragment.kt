package com.sawelo.wordmemorizer.ui.fragment

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.ui.activity.EditWordActivity
import com.sawelo.wordmemorizer.ui.adapter.MainWordAdapter
import com.sawelo.wordmemorizer.ui.adapter.SimilarWordAdapter
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithInfo
import com.sawelo.wordmemorizer.databinding.FragmentWordListBinding
import com.sawelo.wordmemorizer.util.callback.ItemWordAdapterListener
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WordListFragment : Fragment(), ItemWordAdapterListener {
    private val viewModel: MainViewModel by activityViewModels()
    private var binding: FragmentWordListBinding? = null
    private var mainWordAdapter: MainWordAdapter? = null
    private var mainWordRv: RecyclerView? = null
    private var similarWordAdapter: SimilarWordAdapter? = null
    private var similarWordRv: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWordListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setEmptyString()
        setAdapter()
        setRecyclerView()
        setListFromCategory()
    }

    override fun onStart() {
        super.onStart()
        mainWordAdapter?.refresh()
        similarWordAdapter?.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        mainWordAdapter = null
        mainWordRv = null
        similarWordAdapter = null
        similarWordRv = null
    }

    private fun setEmptyString() {
        val string = "Please select your categories\nby clicking the @ icon"
        val spannableString = SpannableString(string)
        val iconIndex = spannableString.indexOf("@")
        spannableString.setSpan(
            ImageSpan(requireContext(), R.drawable.baseline_filter_list_24),
            iconIndex,
            iconIndex + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding?.fragmentCategorySelectCategoriesTv?.text = spannableString
    }

    private fun setAdapter() {
        // Setting adapters
        mainWordAdapter = MainWordAdapter(this).apply {
            addOnPagesUpdatedListener {
                binding?.fragmentCategoryMainWordsProgressIndicator?.hide()
                binding?.fragmentCategorySelectCategoriesTv?.isVisible =
                    mainWordAdapter?.itemCount == 0
            }
        }
        similarWordAdapter = SimilarWordAdapter(this).apply {
            addOnPagesUpdatedListener {
                binding?.fragmentCategorySimilarWordsProgressIndicator?.hide()
            }
        }
    }

    private fun setRecyclerView() {
        // Setting recycler views
        mainWordRv = binding?.fragmentCategoryMainWordsRv?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mainWordAdapter
        }
        similarWordRv = binding?.fragmentCategorySimilarWordsRv?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = similarWordAdapter
        }
    }

    private fun setListFromCategory() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllWordsPagingData().collectLatest {
                binding?.fragmentCategoryMainWordsProgressIndicator?.show()
                mainWordAdapter?.submitData(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllForgottenWordsPagingData().collectLatest {
                binding?.fragmentCategorySimilarWordsProgressIndicator?.show()
                similarWordAdapter?.submitData(it)
            }
        }
    }

    override fun onItemHideBtnClickListener(item: WordWithInfo) {
        viewModel.updateHideForgotWord(item.word)
    }

    override fun onItemForgotBtnClickListener(item: WordWithInfo) {
        viewModel.updateShowForgotWord(item.word)
    }

    override fun onItemLongClickListener(item: WordWithInfo) {
        EditWordActivity.startActivity(
            activity, item.word.wordId
        )
    }
}