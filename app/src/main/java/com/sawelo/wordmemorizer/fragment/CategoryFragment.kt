package com.sawelo.wordmemorizer.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.adapter.CategoryAdapter
import com.sawelo.wordmemorizer.adapter.MainWordAdapter
import com.sawelo.wordmemorizer.data.Category
import com.sawelo.wordmemorizer.data.Word
import com.sawelo.wordmemorizer.databinding.FragmentCategoryBinding
import com.sawelo.wordmemorizer.utils.ItemWordAdapterCallback
import com.sawelo.wordmemorizer.utils.WordCommand
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CategoryFragment : Fragment(), ItemWordAdapterCallback {
    private val viewModel: MainViewModel by activityViewModels()
    private var binding: FragmentCategoryBinding? = null
    private var adapter: MainWordAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var animation: Animation? = null

    private var currentWordCommand: WordCommand? = null
    private var currentWordJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        animation = AnimationUtils.loadAnimation(requireContext(), R.anim.blink_animation)
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onItemForgotBtnClickListener(word: Word) {
        viewModel.updateIsForgottenWord(word, true) {
            println("You forgot this word")
        }
    }

    override fun onItemHideBtnClickListener(word: Word) {
        viewModel.updateIsForgottenWord(word, false) {
            println("You remember this word")
        }
        viewModel.updateForgotCountWord(word) {
            println("Increase forgot count")
        }
    }

    override fun onItemLongClickListener(word: Word) {
        viewModel.deleteWord(word) {
            showToast("Word deleted: ${word.wordText}")
        }
    }

    override fun onItemClickListener(word: Word) {
        println("You're pressing ${word.wordText}")
    }

    override fun onItemListChangedListener(
        previousList: MutableList<Word>,
        currentList: MutableList<Word>
    ) {
        val smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int = SNAP_TO_START
        }

        currentWordJob?.cancel()
        currentWordJob = lifecycleScope.launch {
            val wordList = currentList.minus(previousList.toSet())
            val wordListPosition = currentList.indexOf(wordList.firstOrNull())
            if (wordListPosition != -1 && currentWordCommand != null) {
                recyclerView?.clearOnScrollListeners()
                recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int
                    ) {
                        if (newState == SCROLL_STATE_IDLE) {
                            val viewHolder =
                                recyclerView.findViewHolderForLayoutPosition(wordListPosition)
                                        as? MainWordAdapter.WordViewHolder
                            viewHolder?.itemView?.startAnimation(animation)
                            recyclerView.removeOnScrollListener(this)
                        }
                    }
                })
                smoothScroller.targetPosition = wordListPosition
                recyclerView?.layoutManager?.startSmoothScroll(smoothScroller)
                currentWordCommand = null
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = binding?.fragmentCategoryRv
        adapter = MainWordAdapter(this)

        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = adapter

        @Suppress("DEPRECATION") val parcelable = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable(CategoryAdapter.CATEGORY_ARGS, Category::class.java)
        } else {
            arguments?.getParcelable(CategoryAdapter.CATEGORY_ARGS)
        }

        parcelable?.let { category ->
            viewModel.getAllWordsByCategory(category).observe(viewLifecycleOwner) {
                adapter?.submitList(it)
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
        adapter = null
        recyclerView = null
        animation = null
    }

    private fun showToast(text: String) {
        Toast
            .makeText(requireContext(), text, Toast.LENGTH_SHORT)
            .show()
    }

    fun setWordCommand(wordCommand: WordCommand) {
        currentWordCommand = wordCommand
    }

//    fun observeAdapter(wordCommand: WordCommand) {
//        when (wordCommand) {
//            WordCommand.FORGOT_WORD -> {
//                if (wordId != null && adapter != null && recyclerView != null)
//                    scrollToWordChanged(wordId, adapter!!, recyclerView!!)
//            }
//            WordCommand.INSERT_WORD -> {
//                if (wordId != null && adapter != null && recyclerView != null)
//                    scrollToWordInserted(wordId, adapter!!, recyclerView!!)
//            }
//        }
//
//        adapter.onCurrentListChanged()
//
//        var observer: AdapterDataObserver? = null
//        observer = object : AdapterDataObserver() {
//            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                lifecycleScope.launch {
//                    while (recyclerView?.hasPendingAdapterUpdates() == true) {
//                        delay(200L)
//                    }
//

//
//                    println("SCROLLING TO $positionStart")
//
//                }
//            }
//
//            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
//                lifecycleScope.launch {
//                    while (recyclerView?.hasPendingAdapterUpdates() == true) {
//                        println("DELAYING IN CHANGED")
//                        delay(200L)
//                    }
//
//                    recyclerView?.smoothScrollToPosition(positionStart)
//                    if (adapter?.hasObservers() == true && observer != null) {
//                        println("REMOVING OBSERVER")
//                        adapter?.unregisterAdapterDataObserver(observer!!)
//                    }
//                }
//            }
//        }
//
//        adapter?.registerAdapterDataObserver(observer)
//    }
//
//    fun scrollRecyclerView(wordId: Int?, wordCommand: WordCommand?) {
//        if (wordCommand != null) {
//            when (wordCommand) {
//                WordCommand.FORGOT_WORD -> {
//                    if (wordId != null && adapter != null && recyclerView != null)
//                        scrollToWordChanged(wordId, adapter!!, recyclerView!!)
//                }
//                WordCommand.INSERT_WORD -> {
//                    if (wordId != null && adapter != null && recyclerView != null)
//                        scrollToWordInserted(wordId, adapter!!, recyclerView!!)
//                }
//            }
//        }
//    }

    private fun scrollToWordInserted(
        wordId: Int,
        adapter: MainWordAdapter,
        recyclerView: RecyclerView
    ) {
        lifecycleScope.launch {
            do {
                println("DELAYING IN INSERTED")
                delay(200L)
            } while (recyclerView.hasPendingAdapterUpdates())

            println("CHECK DATA ${adapter.currentList}")
            val wordIndex = adapter.currentList.indexOfFirst { it.id == wordId }

            recyclerView.clearOnScrollListeners()
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int
                ) {
                    if (newState == SCROLL_STATE_IDLE) {
                        val viewHolder = recyclerView.findViewHolderForLayoutPosition(wordIndex)
                                as? MainWordAdapter.WordViewHolder
                        viewHolder?.itemView?.startAnimation(animation)
                        recyclerView.removeOnScrollListener(this)
                    }
                }
            })

            println("SCROLLING TO $wordIndex")
            recyclerView.smoothScrollToPosition(wordIndex)
        }
    }

    private fun scrollToWordChanged(
        wordId: Int,
        adapter: MainWordAdapter,
        recyclerView: RecyclerView
    ) {
        lifecycleScope.launch {
            do {
                println("DELAYING IN INSERTED")
                delay(200L)
            } while (recyclerView.hasPendingAdapterUpdates())

            println("CHECK DATA ${adapter.currentList}")
            val wordIndex = adapter.currentList.indexOfFirst { it.id == wordId }

            println("SCROLLING TO $wordIndex")
            recyclerView.smoothScrollToPosition(wordIndex)
        }
    }
}