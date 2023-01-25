package com.sawelo.wordmemorizer.window

import android.content.Context
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.data_class.Word
import kotlinx.coroutines.CoroutineScope

class FloatingInfoWordWindow(
    private val context: Context,
    private val itemWord: Word,
    private val okPressed: () -> Unit
): DialogWindow(context, R.layout.window_add_word_info_floating) {
    private var wordTv: TextView? = null
    private var furiganaTv: TextView? = null
    private var definitionTv: TextView? = null
    private var okBtn: Button? = null

    override fun setViews(parent: ViewGroup) {
        wordTv = parent.findViewById(R.id.info_wordText)
        furiganaTv = parent.findViewById(R.id.info_furiganaText)
        definitionTv = parent.findViewById(R.id.info_definitionText)
        okBtn = parent.findViewById(R.id.info_ok)
    }

    override fun clearViews() {
        wordTv = null
        furiganaTv = null
        definitionTv = null
        okBtn = null
    }

    override fun beforeShowWindow(coroutineScope: CoroutineScope) {
        setWidth(context.resources.displayMetrics.widthPixels - 200)

        wordTv?.text = itemWord.wordText
        furiganaTv?.text = itemWord.furiganaText
        definitionTv?.text = itemWord.definitionText
        okBtn?.setOnClickListener {
            closeWindow()
            okPressed.invoke()
        }
    }

    override fun beforeCloseWindow(coroutineScope: CoroutineScope) {}

}