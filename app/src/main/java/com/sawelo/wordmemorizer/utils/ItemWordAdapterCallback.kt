package com.sawelo.wordmemorizer.utils

import com.sawelo.wordmemorizer.data.Word

interface ItemWordAdapterCallback {
    fun onItemForgotBtnClickListener(word: Word) {}
    fun onItemHideBtnClickListener(word: Word) {}
    fun onItemLongClickListener(word: Word) {}
    fun onItemClickListener(word: Word)
}