package com.sawelo.wordmemorizer.util.callback

import androidx.recyclerview.widget.DiffUtil
import com.sawelo.wordmemorizer.data.data_class.Word

object WordDiffUtilCallback: DiffUtil.ItemCallback<Word>() {
    override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
        return oldItem == newItem
    }
}