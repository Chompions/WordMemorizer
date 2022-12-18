package com.sawelo.wordmemorizer

import androidx.recyclerview.widget.DiffUtil
import com.sawelo.wordmemorizer.data.Word

object DiffUtilCallback: DiffUtil.ItemCallback<Word>() {
    override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
        return oldItem.kanjiText == newItem.kanjiText
    }

    override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
        return oldItem == newItem
    }
}