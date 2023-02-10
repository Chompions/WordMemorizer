package com.sawelo.wordmemorizer.util.diff_util

import androidx.recyclerview.widget.DiffUtil
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithInfo

object WordDiffUtilCallback: DiffUtil.ItemCallback<WordWithInfo>() {
    override fun areItemsTheSame(oldItem: WordWithInfo, newItem: WordWithInfo): Boolean {
        return oldItem.word.wordId == newItem.word.wordId
    }

    override fun areContentsTheSame(oldItem: WordWithInfo, newItem: WordWithInfo): Boolean {
        return oldItem == newItem
    }
}