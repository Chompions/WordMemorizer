package com.sawelo.wordmemorizer.ui.adapter

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

class SimilarWordAdapter(
    private val itemCallback: ItemWordAdapterListener
) : PagingDataAdapter<WordWithInfo, SimilarWordAdapter.WordViewHolder>(WordDiffUtilCallback) {

    inner class WordViewHolder(itemView: View) : ViewHolder(itemView) {
        val mainWord: TextView = itemView.findViewById(R.id.itemWord_mainWord_tv)
        val furiganaWord: TextView = itemView.findViewById(R.id.itemWord_furiganaWord_tv)
        val definitionWord: TextView = itemView.findViewById(R.id.itemWord_definitionWord_tv)
        val hideBtn: Button = itemView.findViewById(R.id.itemWord_hide_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_similar_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        getItem(position)?.let { itemWord ->
            with(holder) {
                mainWord.text = itemWord.word.wordText
                furiganaWord.text = itemWord.word.furiganaText
                definitionWord.text = itemWord.word.definitionText

                hideBtn.setOnClickListener {
                    itemCallback.onItemHideBtnClickListener(itemWord)
                }

                itemView.setOnClickListener {
                    itemCallback.onItemClickListener(itemWord)
                }

                itemView.setOnLongClickListener {
                    itemCallback.onItemLongClickListener(itemWord)
                    true
                }
            }
        }
    }
}