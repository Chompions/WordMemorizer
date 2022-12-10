package com.sawelo.wordmemorizer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sawelo.wordmemorizer.data.Word

class WordAdapter(
    private val callback: WordAdapterCallback
) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    private var words = emptyList<Word>()

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
        with(holder) {
            kanjiWord.text = words[position].kanjiText
            hiraganaWord.text = words[position].hiraganaText
            definitionWord.text = words[position].definitionText
            forgotCount.text = words[position].forgotCount.toString()

            forgotBtn.setOnClickListener {
                detailLayout.visibility = View.VISIBLE
                forgotBtn.isEnabled = false
            }

            hideBtn.setOnClickListener {
                detailLayout.visibility = View.GONE
                forgotBtn.isEnabled = true
                callback.onItemHideBtnClickListener(words[layoutPosition])
            }

            itemView.setOnLongClickListener {
                callback.onItemLongClickListener(words[layoutPosition])
                true
            }
        }
    }

    override fun getItemCount(): Int = words.size

    fun notifyInsertAllWords(newWords: List<Word>) {
        this.words = newWords
        notifyItemRangeInserted(0, newWords.size)
    }

    fun notifyInsertNewWord(kanji: String, newWords: List<Word>, recyclerView: RecyclerView) {
        this.words = newWords
        val finalPosition = newWords.indexOfFirst {it.kanjiText == kanji}
        notifyItemInserted(finalPosition)
        recyclerView.smoothScrollToPosition(finalPosition)
    }

    fun notifyDeleteWord(kanji: String, newWords: List<Word>) {
        val initialPosition = this.words.indexOfFirst {it.kanjiText == kanji}
        notifyItemRemoved(initialPosition)
        this.words = newWords
    }

    fun notifyChangeWord(kanji: String, newWords: List<Word>, recyclerView: RecyclerView) {
        val initialPosition = this.words.indexOfFirst {it.kanjiText == kanji}
        val finalPosition = newWords.indexOfFirst {it.kanjiText == kanji}
        this.words = newWords

        if (finalPosition < initialPosition) {
            notifyItemMoved(initialPosition, finalPosition)
            notifyItemRangeChanged(finalPosition, (itemCount - finalPosition))
        } else {
            notifyItemChanged(finalPosition)
        }
        recyclerView.smoothScrollToPosition(finalPosition)
    }
}