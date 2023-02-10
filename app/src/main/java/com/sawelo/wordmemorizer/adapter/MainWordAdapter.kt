package com.sawelo.wordmemorizer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithInfo
import com.sawelo.wordmemorizer.util.callback.ItemWordAdapterListener
import com.sawelo.wordmemorizer.util.diff_util.WordDiffUtilCallback

class MainWordAdapter(
    private val itemCallback: ItemWordAdapterListener
) : PagingDataAdapter<WordWithInfo, MainWordAdapter.WordViewHolder>(WordDiffUtilCallback) {

    inner class WordViewHolder(itemView: View) : ViewHolder(itemView) {
        val mainWord: TextView = itemView.findViewById(R.id.itemWord_mainWord_tv)
        val rememberCount: TextView = itemView.findViewById(R.id.itemWord_rememberCount_tv)
        val forgotBtn: Button = itemView.findViewById(R.id.itemWord_forgot_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_main_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        getItem(position)?.let { wordItem ->
            with(holder) {
                mainWord.text = wordItem.word.wordText
                rememberCount.text = wordItem.wordInfo.rememberCount.toString()
                forgotBtn.isEnabled = !wordItem.wordInfo.isForgotten

                forgotBtn.setOnClickListener {
                    itemCallback.onItemForgotBtnClickListener(wordItem)
                }

                itemView.setOnClickListener {
                    itemCallback.onItemClickListener(wordItem)
                }

                itemView.setOnLongClickListener {
                    itemCallback.onItemLongClickListener(wordItem)
                    true
                }
            }
        }
    }
}