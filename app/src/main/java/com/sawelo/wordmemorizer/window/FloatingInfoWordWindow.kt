package com.sawelo.wordmemorizer.window

import android.content.Context
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.activity.EditWordActivity
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.util.FloatingUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class FloatingInfoWordWindow(
    private val context: Context,
    private val wordRepository: WordRepository,
    private val itemWord: Word
): DialogWindow(context, R.layout.window_add_word_info_floating) {
    private var wordTv: TextView? = null
    private var furiganaTv: TextView? = null
    private var definitionTv: TextView? = null
    private var okBtn: Button? = null

    private var floatingUtils: FloatingUtils? = null

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
        floatingUtils = FloatingUtils(wordRepository)
        setWidth(context.resources.displayMetrics.widthPixels - 200)

        wordTv?.text = itemWord.wordText
        furiganaTv?.text = itemWord.furiganaText
        definitionTv?.text = itemWord.definitionText
        wordTv?.setOnLongClickListener {
            coroutineScope.launch {
                val categoryList = floatingUtils?.getAllCategories()
                if (categoryList != null) {
                    EditWordActivity.startActivity(
                        context, itemWord.wordId, categoryList
                    )
                }
                closeWindow()
            }
            true
        }
        okBtn?.setOnClickListener {
            closeWindow()
        }
    }

    override fun beforeCloseWindow(coroutineScope: CoroutineScope) {}
    override fun setAdditionalParams(params: WindowManager.LayoutParams?) {}
}