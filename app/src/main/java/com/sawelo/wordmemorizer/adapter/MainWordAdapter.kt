package com.sawelo.wordmemorizer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.card.MaterialCardView
import com.sawelo.wordmemorizer.utils.DiffUtilCallback
import com.sawelo.wordmemorizer.utils.ItemWordAdapterCallback
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.utils.WordUtils.getColorFromAttr
import com.sawelo.wordmemorizer.data.Word

class MainWordAdapter(
    private val itemCallback: ItemWordAdapterCallback
) : ListAdapter<Word, MainWordAdapter.WordViewHolder>(DiffUtilCallback) {

    inner class WordViewHolder(itemView: View) : ViewHolder(itemView) {
        val kanjiWord: TextView = itemView.findViewById(R.id.itemWord_similarWord_tv)
        val forgotCount: TextView = itemView.findViewById(R.id.itemWord_forgotCount_tv)
        val forgotBtn: Button = itemView.findViewById(R.id.itemWord_forgot_btn)

        val detailLayout: ConstraintLayout = itemView.findViewById(R.id.itemWord_detail_cl)
        val hiraganaWord: TextView = itemView.findViewById(R.id.itemWord_hiraganaWord_tv)
        val definitionWord: TextView = itemView.findViewById(R.id.itemWord_definitionWord_tv)
        val hideBtn: Button = itemView.findViewById(R.id.itemWord_hide_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_main_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val itemWord = getItem(position)
        with(holder) {
            kanjiWord.text = itemWord.wordText
            hiraganaWord.text = itemWord.furiganaText
            definitionWord.text = itemWord.definitionText
            forgotCount.text = itemWord.forgotCount.toString()

            if (itemWord.isForgotten) {
                with(itemView) {
                    val color =
                        context.getColorFromAttr(com.google.android.material.R.attr.colorError)
                    (this as MaterialCardView).strokeColor = color
                }
                detailLayout.visibility = View.VISIBLE
                forgotBtn.isEnabled = false
            } else {
                with(itemView) {
                    val color = context.getColor(android.R.color.transparent)
                    (this as MaterialCardView).strokeColor = color
                }
                detailLayout.visibility = View.GONE
                forgotBtn.isEnabled = true
            }

            forgotBtn.setOnClickListener {
                itemCallback.onItemForgotBtnClickListener(itemWord)
            }

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