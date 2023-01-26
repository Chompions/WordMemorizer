package com.sawelo.wordmemorizer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.util.callback.ItemWordAdapterListener
import com.sawelo.wordmemorizer.util.callback.WordDiffUtilCallback

class AddWordAdapter(
    private val itemCallback: ItemWordAdapterListener
) : ListAdapter<Word, AddWordAdapter.WordViewHolder>(WordDiffUtilCallback) {

    inner class WordViewHolder(itemView: View) : ViewHolder(itemView) {
        val similarWord: TextView = itemView.findViewById(R.id.itemWord_similarWord_tv)
        val forgotCount: TextView = itemView.findViewById(R.id.itemWord_forgotCount_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val itemWord = getItem(position)
        with(holder) {
            similarWord.text = itemWord.wordText
            forgotCount.text = itemWord.forgotCount.toString()

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