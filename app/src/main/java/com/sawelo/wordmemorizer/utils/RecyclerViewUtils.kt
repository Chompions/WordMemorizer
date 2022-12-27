package com.sawelo.wordmemorizer.utils

import androidx.recyclerview.widget.RecyclerView
import com.sawelo.wordmemorizer.adapter.MainWordAdapter

object RecyclerViewUtils {
    fun scrollToWordId(
        wordId: Int?,
        mainWordAdapter: MainWordAdapter?,
        recyclerView: RecyclerView?,
        onScrolled: ((viewHolder: RecyclerView.ViewHolder) -> Unit)
    ) {
        if (wordId != null && mainWordAdapter != null && recyclerView != null) {
            println("PREPARING TO SCROLL IN SCROLL TO WORD")
            recyclerView.stopScroll()
            recyclerView.clearOnScrollListeners()
            println("DOING ON THE NEXT LAYOUT")
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
                            println("INVOKING STUFF")
                            onScrolled.invoke(viewHolder!!)
                        }
                        recyclerView.clearOnScrollListeners()
                    }
                }
                println("ADDING LISTENER")
                recyclerView.addOnScrollListener(recyclerViewScrollListener)
                println("SCROLLING TO POSITION")
                recyclerView.smoothScrollToPosition(wordIndex)
            } else {
                if (viewHolder != null) {
                    onScrolled.invoke(viewHolder!!)
                }
            }
        }
    }
}