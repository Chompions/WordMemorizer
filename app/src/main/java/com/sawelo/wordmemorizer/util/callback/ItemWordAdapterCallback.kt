package com.sawelo.wordmemorizer.util.callback

import com.sawelo.wordmemorizer.data.data_class.Word

interface ItemWordAdapterCallback {
    fun onItemForgotBtnClickListener(word: Word) {}
    fun onItemHideBtnClickListener(word: Word) {}
    fun onItemLongClickListener(word: Word) {}
    fun onItemClickListener(word: Word) {}
}