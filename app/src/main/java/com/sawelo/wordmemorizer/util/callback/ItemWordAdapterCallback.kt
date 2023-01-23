package com.sawelo.wordmemorizer.util.callback

import com.sawelo.wordmemorizer.data.data_class.Word

interface ItemWordAdapterCallback {
    fun onItemForgotBtnClickListener(item: Word) {}
    fun onItemHideBtnClickListener(item: Word) {}
    fun onItemLongClickListener(item: Word) {}
    fun onItemClickListener(item: Word) {}
}