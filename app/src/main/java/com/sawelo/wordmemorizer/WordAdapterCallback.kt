package com.sawelo.wordmemorizer

import com.sawelo.wordmemorizer.data.Word

interface WordAdapterCallback {
    fun onItemHideBtnClickListener(word: Word)
    fun onItemLongClickListener(word: Word)
}