package com.sawelo.wordmemorizer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sawelo.wordmemorizer.data.Word

class WordAdapter(
    private val itemCallback: ItemWordAdapterCallback
) : ListAdapter<Word, WordAdapter.WordViewHolder>(DiffUtilCallback) {

    inner class WordViewHolder(itemView: View) : ViewHolder(itemView) {
        val kanjiWord: TextView = itemView.findViewById(R.id.itemWord_kanjiWord_tv)
        val forgotCount: TextView = itemView.findViewById(R.id.itemWord_forgotCount_tv)
        val forgotBtn: Button = itemView.findViewById(R.id.itemWord_forgot_btn)

        val detailLayout: ConstraintLayout = itemView.findViewById(R.id.itemWord_detail_cl)
        val hiraganaWord: TextView = itemView.findViewById(R.id.itemWord_hiraganaWord_tv)
        val definitionWord: TextView = itemView.findViewById(R.id.itemWord_definitionWord_tv)
        val hideBtn: Button = itemView.findViewById(R.id.itemWord_hide_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val itemWord = getItem(position)
        with(holder) {
            kanjiWord.text = itemWord.kanjiText
            hiraganaWord.text = itemWord.hiraganaText
            definitionWord.text = itemWord.definitionText
            forgotCount.text = itemWord.forgotCount.toString()

            forgotBtn.setOnClickListener {
                detailLayout.visibility = View.VISIBLE
                forgotBtn.isEnabled = false
            }

            hideBtn.setOnClickListener {
                detailLayout.visibility = View.GONE
                forgotBtn.isEnabled = true
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