package com.sawelo.wordmemorizer.utils

import androidx.recyclerview.widget.DiffUtil
import com.sawelo.wordmemorizer.data.Word

object WordDiffUtilCallback: DiffUtil.ItemCallback<Word>() {
    override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
        return oldItem == newItem
    }
}