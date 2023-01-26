package com.sawelo.wordmemorizer.window

import android.content.Context
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.adapter.AddWordAdapter
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import com.sawelo.wordmemorizer.util.FloatingAddWordUtils
import com.sawelo.wordmemorizer.util.ViewUtils.addButtonInLayout
import com.sawelo.wordmemorizer.util.ViewUtils.addCategoryList
import com.sawelo.wordmemorizer.util.callback.ItemWordAdapterListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FloatingAddWordWindow(
    private val context: Context,
    private val wordRepository: WordRepository
) : DialogWindow(context, R.layout.window_add_word_floating),
    ItemWordAdapterListener {

    private var wordEt: EditText? = null
    private var drawWordBtn: ImageButton? = null
    private var furiganaEt: EditText? = null
    private var definitionEt: EditText? = null
    private var similarWordRv: RecyclerView? = null
    private var similarWordTv: TextView? = null
    private var progressIndicator: LinearProgressIndicator? = null
    private var recommendationLayout: LinearLayout? = null
    private var addCategoryGroup: MaterialButtonToggleGroup? = null
    private var addBtn: Button? = null
    private var cancelBtn: Button? = null

    private var coroutineScope: CoroutineScope? = null
    private var adapter: AddWordAdapter? = null
    private var floatingAddWordUtils: FloatingAddWordUtils? = null
    private var categoryList: List<Category>? = null

    override fun setViews(parent: ViewGroup) {
        wordEt = parent.findViewById(R.id.dialog_addWord_et)
        drawWordBtn = parent.findViewById(R.id.dialog_drawWord_btn)
        furiganaEt = parent.findViewById(R.id.dialog_addFurigana_et)
        definitionEt = parent.findViewById(R.id.dialog_addDefinition_et)
        similarWordRv = parent.findViewById(R.id.dialog_similarWord_rv)
        similarWordTv = parent.findViewById(R.id.dialog_similarWord_tv)
        progressIndicator = parent.findViewById(R.id.dialog_addCategory_progressIndicator)
        recommendationLayout = parent.findViewById(R.id.dialog_addCategory_recommendationLayout)
        addCategoryGroup = parent.findViewById(R.id.dialog_addCategory_group)
        addBtn = parent.findViewById(R.id.dialog_addWord_btn)
        cancelBtn = parent.findViewById(R.id.dialog_cancel_btn)
    }

    override fun clearViews() {
        wordEt = null
        drawWordBtn = null
        furiganaEt = null
        definitionEt = null
        similarWordRv = null
        similarWordTv = null
        progressIndicator = null
        recommendationLayout = null
        addCategoryGroup = null
        addBtn = null
        cancelBtn = null
    }

    override fun beforeShowWindow(coroutineScope: CoroutineScope) {
        this.coroutineScope = coroutineScope
        floatingAddWordUtils = FloatingAddWordUtils(wordRepository)

        setAdapter()
        setDrawWindow()
        setWordsChangeListener()
        setCategoryList()
        setButton()

        IS_WINDOW_ACTIVE = true
    }

    private fun setAdapter() {
        adapter = AddWordAdapter(this)
        similarWordRv?.adapter = adapter
        similarWordRv?.layoutManager = LinearLayoutManager(context)
    }

    private fun setDrawWindow() {
        drawWordBtn?.setOnClickListener {
            FloatingDrawWordWindow(context) {
                wordEt!!.setText(it)
            }.showWindow()
        }
    }

    private fun setWordsChangeListener() {
        wordEt?.doOnTextChanged { text, _, _, _ ->
            floatingAddWordUtils?.wordTextFlow?.value = text.toString()
        }
        furiganaEt?.doOnTextChanged { text, _, _, _ ->
            floatingAddWordUtils?.furiganaTextFlow?.value = text.toString()
        }
        definitionEt?.doOnTextChanged { text, _, _, _ ->
            floatingAddWordUtils?.definitionTextFlow?.value = text.toString()
        }

        coroutineScope?.launch {
            floatingAddWordUtils?.progressIndicatorShowFlow?.collectLatest {
                progressIndicator?.isVisible = it
            }
        }

        coroutineScope?.launch {
            floatingAddWordUtils?.getAllWordsByTextFlow()?.collectLatest {
                similarWordTv?.isVisible = it.isEmpty()
                adapter?.submitList(it)
            }
        }

        coroutineScope?.launch {
            try {
                floatingAddWordUtils?.getRecommendationWordsFlow()
                    ?.collectLatest {
                        recommendationLayout?.removeAllViews()
                        it.forEach { data ->
                            val wordText = data.japanese.first().word
                            val furiganaText = data.japanese.first().reading
                            val definitionText =
                                data.senses.first().englishDefinitions.joinToString(" / ")
                            recommendationLayout?.addButtonInLayout(context, wordText) {
                                wordEt?.setText(wordText)
                                furiganaEt?.setText(furiganaText)
                                definitionEt?.setText(definitionText)
                            }
                        }
                    }
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                showToast("Obtaining recommended words failed: ${e.message}")
            }
        }
    }

    private fun setCategoryList() {
        runBlocking {
            categoryList = floatingAddWordUtils?.getAllCategories()
            if (categoryList != null) {
                addCategoryGroup?.addCategoryList(context, categoryList!!)
            }
        }
    }

    private fun setButton() {
        addBtn?.setOnClickListener {
            val wordWithCategories = WordWithCategories(
                Word(
                    wordText = wordEt?.text.toString(),
                    furiganaText = furiganaEt?.text.toString(),
                    definitionText = definitionEt?.text.toString(),
                    createdTimeMillis = System.currentTimeMillis(),
                ),
                categoryList!!.filter {
                    it.categoryId in (addCategoryGroup?.checkedButtonIds ?: emptyList())
                }
            )
            when {
                wordWithCategories.word.wordText.isBlank() -> showToast("Word cannot be empty")
                wordWithCategories.word.furiganaText.isBlank() -> showToast("Furigana cannot be empty")
                wordWithCategories.word.definitionText.isBlank() -> showToast("Definition cannot be empty")
                else -> {
                    coroutineScope?.launch {
                        floatingAddWordUtils?.addWord(wordWithCategories)
                        closeWindow()
                    }
                }
            }
        }
        cancelBtn?.setOnClickListener {
            closeWindow()
        }
    }

    override fun beforeCloseWindow(coroutineScope: CoroutineScope) {
        floatingAddWordUtils = null
        IS_WINDOW_ACTIVE = false
    }

    override fun onItemClickListener(item: Word) {
        coroutineScope?.launch {
            floatingAddWordUtils?.updateShowForgotWord(item)
            FloatingInfoWordWindow(context, item).showWindow()
            closeWindow()
        }
    }

    private fun showToast(text: String) {
        Toast
            .makeText(context, text, Toast.LENGTH_SHORT)
            .show()
    }

    companion object {
        private var IS_WINDOW_ACTIVE = false
        fun getIsWindowActive() = IS_WINDOW_ACTIVE
    }
}