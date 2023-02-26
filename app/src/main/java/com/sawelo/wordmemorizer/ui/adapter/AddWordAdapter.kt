package com.sawelo.wordmemorizer.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithInfo
import com.sawelo.wordmemorizer.util.callback.ItemWordAdapterListener
import com.sawelo.wordmemorizer.util.diff_util.WordDiffUtilCallback

class AddWordAdapter(
    private val itemCallback: ItemWordAdapterListener
) : ListAdapter<WordWithInfo, AddWordAdapter.WordViewHolder>(WordDiffUtilCallback) {

    inner class WordViewHolder(itemView: View) : ViewHolder(itemView) {
        val similarWord: TextView = itemView.findViewById(R.id.itemWord_similarWord_tv)
        val rememberCount: TextView = itemView.findViewById(R.id.itemWord_rememberCount_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val itemWord = getItem(position)
        with(holder) {
            similarWord.text = itemWord.word.wordText
            rememberCount.text = itemWord.wordInfo.rememberCount.toString()

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