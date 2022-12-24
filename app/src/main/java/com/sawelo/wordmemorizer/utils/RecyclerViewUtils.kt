package com.sawelo.wordmemorizer.utils

import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.RecyclerView
import com.sawelo.wordmemorizer.adapter.MainWordAdapter

object RecyclerViewUtils {
    fun scrollToWordId(
        wordId: Int,
        mainWordAdapter: MainWordAdapter?,
        recyclerView: RecyclerView?,
        onScrolled: ((viewHolder: RecyclerView.ViewHolder) -> Unit)
    ) {
        if (mainWordAdapter != null && recyclerView != null) {
            recyclerView.doOnNextLayout {
                val word = mainWordAdapter.currentList.first { it.id == wordId }
                val wordIndex = mainWordAdapter.currentList.indexOf(word)

                var viewHolder = recyclerView.findViewHolderForLayoutPosition(wordIndex)
                        as? MainWordAdapter.WordViewHolder

                val isVisible = if (viewHolder != null) {
                    recyclerView.layoutManager?.isViewPartiallyVisible(
                        viewHolder.itemView,
                        true,
                        false
                    ) ?: false
                } else false

                if (!isVisible) {
                    val recyclerViewScrollListener = object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            viewHolder = recyclerView.findViewHolderForLayoutPosition(wordIndex)
                                    as? MainWordAdapter.WordViewHolder
                            if (viewHolder != null) {
                                onScrolled.invoke(viewHolder!!)
                            }
                            recyclerView.clearOnScrollListeners()
                        }
                    }

                    recyclerView.smoothScrollToPosition(wordIndex)
                    recyclerView.addOnScrollListener(recyclerViewScrollListener)
                } else {
                    if (viewHolder != null) {
                        onScrolled.invoke(viewHolder!!)
                    }
                }
            }
        }
    }
}