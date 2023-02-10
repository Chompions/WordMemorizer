package com.sawelo.wordmemorizer.util.callback

import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithInfo

interface ItemWordAdapterListener {
    fun onItemForgotBtnClickListener(item: WordWithInfo) {}
    fun onItemHideBtnClickListener(item: WordWithInfo) {}
    fun onItemLongClickListener(item: WordWithInfo) {}
    fun onItemClickListener(item: WordWithInfo) {}
}