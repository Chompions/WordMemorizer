package com.sawelo.wordmemorizer

import com.sawelo.wordmemorizer.data.Word

interface ItemWordAdapterCallback {
    fun onItemHideBtnClickListener(word: Word)
    fun onItemLongClickListener(word: Word)
    fun onItemClickListener(word: Word)
}