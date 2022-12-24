package com.sawelo.wordmemorizer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.adapter.MainWordAdapter
import com.sawelo.wordmemorizer.data.Word
import com.sawelo.wordmemorizer.databinding.FragmentCategoryBinding
import com.sawelo.wordmemorizer.utils.EndOffsetItemDecoration
import com.sawelo.wordmemorizer.utils.ItemWordAdapterCallback
import com.sawelo.wordmemorizer.utils.RecyclerViewUtils.scrollToWordId
import com.sawelo.wordmemorizer.utils.WordCommand
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import kotlinx.coroutines.runBlocking

class CategoryFragment : Fragment(), ItemWordAdapterCallback {
    private val viewModel: MainViewModel by activityViewModels()
    private var binding: FragmentCategoryBinding? = null
    private var adapter: MainWordAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var animation: Animation? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        animation = AnimationUtils.loadAnimation(requireContext(), R.anim.blink_animation)
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onItemForgotBtnClickListener(word: Word) {
        runBlocking {
            viewModel.setIsForgottenWord(word, true)
        }
    }

    override fun onItemHideBtnClickListener(word: Word) {
        runBlocking {
            viewModel.setIsForgottenWord(word, false)
            viewModel.setForgotCountIncreaseWord(word) {
                showToast("Word forgotten: ${word.wordText}")
            }
        }
    }

    override fun onItemLongClickListener(word: Word) {
        runBlocking {
            viewModel.deleteWord(word) {
                showToast("Word deleted: ${word.wordText}")
            }
        }
    }

    override fun onItemClickListener(word: Word) {
        println("You're pressing ${word.wordText}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = binding?.fragmentCategoryRv
        adapter = MainWordAdapter(this)

        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.addItemDecoration(EndOffsetItemDecoration(500))
        recyclerView?.adapter = adapter

        viewModel.allWordListLiveData.observe(viewLifecycleOwner) {
            adapter?.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        adapter = null
        recyclerView = null
        animation = null
    }

    private fun showToast(text: String) {
        Toast
            .makeText(requireContext(), text, Toast.LENGTH_SHORT)
            .show()
    }

    fun scrollRecyclerView(wordId: Int, wordCommand: WordCommand) {
        when (wordCommand) {
            WordCommand.FORGOT_WORD -> {
                scrollToWordId(wordId, adapter, recyclerView) { viewHolder ->
                    (viewHolder as MainWordAdapter.WordViewHolder).forgotBtn.performClick()
                }
            }
            WordCommand.INSERT_WORD -> {
                scrollToWordId(wordId, adapter, recyclerView) { viewHolder ->
                    viewHolder.itemView.startAnimation(animation)
                }
            }
        }
    }
}