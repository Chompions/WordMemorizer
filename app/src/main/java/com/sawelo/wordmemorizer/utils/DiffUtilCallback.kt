package com.sawelo.wordmemorizer.utils

import androidx.recyclerview.widget.DiffUtil
import com.sawelo.wordmemorizer.data.Word

object DiffUtilCallback: DiffUtil.ItemCallback<Word>() {
    override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
        return oldItem.wordText == newItem.wordText
    }

    override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
        return oldItem == newItem
    }
}