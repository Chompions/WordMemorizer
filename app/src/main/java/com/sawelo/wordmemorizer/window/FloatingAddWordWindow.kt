package com.sawelo.wordmemorizer.window

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.activity.EditWordActivity
import com.sawelo.wordmemorizer.adapter.AddWordAdapter
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import com.sawelo.wordmemorizer.util.Constants.isAddWordWindowActive
import com.sawelo.wordmemorizer.util.FloatingAddWordUtils
import com.sawelo.wordmemorizer.util.ViewUtils.addButtonInLayout
import com.sawelo.wordmemorizer.util.ViewUtils.addCategoryList
import com.sawelo.wordmemorizer.util.callback.ItemWordAdapterListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest


class FloatingAddWordWindow(
    private val context: Context,
    private val wordRepository: WordRepository,
    private val currentCategory: Category?,
) : DialogWindow(context, R.layout.window_add_word_floating),
    ItemWordAdapterListener {

    private val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    private var wordEt: TextInputEditText? = null
    private var furiganaEt: TextInputEditText? = null
    private var definitionEt: TextInputEditText? = null

    private var wordIl: TextInputLayout? = null
    private var furiganaIl: TextInputLayout? = null
    private var definitionIl: TextInputLayout? = null

    private var drawWordBtn: ImageButton? = null
    private var similarWordRv: RecyclerView? = null
    private var similarWordTv: TextView? = null

    private var progressIndicator: LinearProgressIndicator? = null
    private var recommendationLayout: LinearLayout? = null
    private var searchWordBtn: Button? = null

    private var addCategoryGroup: MaterialButtonToggleGroup? = null
    private var addBtn: Button? = null
    private var cancelBtn: Button? = null

    private var coroutineScope: CoroutineScope? = null
    private var searchJob: Job? = null
    private var adapter: AddWordAdapter? = null
    private var floatingAddWordUtils: FloatingAddWordUtils? = null
    private var categoryList: List<Category>? = null

    override fun setViews(parent: ViewGroup) {
        wordEt = parent.findViewById(R.id.dialog_addWord_et)
        furiganaEt = parent.findViewById(R.id.dialog_addFurigana_et)
        definitionEt = parent.findViewById(R.id.dialog_addDefinition_et)

        wordIl = parent.findViewById(R.id.dialog_addWord_il)
        furiganaIl = parent.findViewById(R.id.dialog_addFurigana_il)
        definitionIl = parent.findViewById(R.id.dialog_addDefinition_il)

        drawWordBtn = parent.findViewById(R.id.dialog_drawWord_btn)
        similarWordRv = parent.findViewById(R.id.dialog_similarWord_rv)
        similarWordTv = parent.findViewById(R.id.dialog_similarWord_tv)

        progressIndicator = parent.findViewById(R.id.dialog_progressIndicator)
        recommendationLayout = parent.findViewById(R.id.dialog_recommendationLayout)
        searchWordBtn = parent.findViewById(R.id.dialog_searchWord_btn)

        addCategoryGroup = parent.findViewById(R.id.dialog_addCategory_group)
        addBtn = parent.findViewById(R.id.dialog_addWord_btn)
        cancelBtn = parent.findViewById(R.id.dialog_cancel_btn)
    }

    override fun clearViews() {
        wordEt = null
        furiganaEt = null
        definitionEt = null

        wordIl = null
        furiganaIl = null
        definitionIl = null

        drawWordBtn = null
        similarWordRv = null
        similarWordTv = null

        progressIndicator = null
        recommendationLayout = null
        searchWordBtn = null

        addCategoryGroup = null
        addBtn = null
        cancelBtn = null
    }

    override fun beforeShowWindow(coroutineScope: CoroutineScope) {
        this.coroutineScope = coroutineScope

        /**
         * All functionality that involves repository should only be managed through Utils
         */
        floatingAddWordUtils = FloatingAddWordUtils(wordRepository)

        setAdapter()
        setDrawWindow()
        setWordsChangeListener()
        setCategoryList()
        setButton()

        isAddWordWindowActive = true
    }

    private fun setAdapter() {
        adapter = AddWordAdapter(this)
        similarWordRv?.adapter = adapter
        similarWordRv?.layoutManager = LinearLayoutManager(context)
    }

    private fun setDrawWindow() {
        drawWordBtn?.setOnClickListener {
            FloatingDrawWordWindow(context, wordEt?.text.toString(), this) {
                wordEt!!.setText(it)
            }.showWindow()
            hideWindow()
        }
    }

    private fun TextInputLayout.checkCopyOrPaste() {
        when {
            editText?.text.isNullOrBlank() && clipboardManager.primaryClipDescription
                ?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true -> {
                isEndIconVisible = true
                setEndIconDrawable(R.drawable.baseline_content_paste_24)
                setEndIconOnClickListener {
                    val clipData = clipboardManager.primaryClip
                    val pastedText = clipData?.getItemAt(0)?.coerceToText(context)
                    this.editText?.setText(pastedText)
                    showToast("Text pasted")
                }
            }
            !editText?.text.isNullOrBlank() -> {
                isEndIconVisible = true
                setEndIconDrawable(R.drawable.baseline_content_copy_24)
                setEndIconOnClickListener {
                    val clipData = ClipData.newPlainText("word", this.editText?.text)
                    clipboardManager.setPrimaryClip(clipData)
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) showToast("Text copied")
                }
            }
            else -> isEndIconVisible = false
        }
    }

    private fun TextInputLayout.setListener(inputType: FloatingAddWordUtils.InputType) {
        editText?.setOnFocusChangeListener { _, isFocused ->
            resetWordRecommendation()
            floatingAddWordUtils?.focusedTextInput = inputType
            searchWordBtn?.isEnabled = !editText?.text.isNullOrBlank()

            checkCopyOrPaste()
            isEndIconVisible = isFocused
        }

        editText?.doOnTextChanged { text, _, _, _ ->
            resetWordRecommendation()
            floatingAddWordUtils?.setWordFlow(inputType, text.toString())
            searchWordBtn?.isEnabled = !editText?.text.isNullOrBlank()

            checkCopyOrPaste()
        }
    }

    override fun setAdditionalParams(params: WindowManager.LayoutParams?) {}

    private fun setWordsChangeListener() {
        wordEt?.requestFocus()

        wordIl?.setListener(FloatingAddWordUtils.InputType.WORD_INPUT)
        furiganaIl?.setListener(FloatingAddWordUtils.InputType.FURIGANA_INPUT)
        definitionIl?.setListener(FloatingAddWordUtils.InputType.DEFINITION_INPUT)

        coroutineScope?.launch {
            floatingAddWordUtils?.getAllWordsByTextFlow()?.collectLatest {
                similarWordTv?.isVisible = it.isEmpty()
                adapter?.submitList(it)
            }
        }
    }

    private fun setCategoryList() {
        runBlocking {
            categoryList = floatingAddWordUtils?.getAllCategories()
            if (categoryList != null) {
                addCategoryGroup?.addCategoryList(context, categoryList!!)
            }
            if (currentCategory != null) {
                addCategoryGroup?.check(currentCategory.categoryId)
            }
        }
    }

    private fun setButton() {
        searchWordBtn?.setOnClickListener {
            searchWordBtn?.visibility = View.INVISIBLE
            searchWordRecommendations()
        }

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

    private fun searchWordRecommendations() {
        progressIndicator?.isVisible = true
        searchJob = coroutineScope?.launch {
            try {
                recommendationLayout?.removeAllViews()
                val recommendedWords = floatingAddWordUtils?.getRecommendationsWords()
                if (!recommendedWords.isNullOrEmpty()) {
                    recommendedWords.forEach { data ->
                        val wordText = data.japanese?.first()?.word
                        val furiganaText = data.japanese?.first()?.reading
                        val definitionText =
                            data.senses?.first()?.englishDefinitions?.joinToString(" / ")
                        if (!wordText.isNullOrBlank()) {
                            recommendationLayout?.addButtonInLayout(context, wordText) {
                                wordEt?.setText(wordText)
                                furiganaEt?.setText(furiganaText)
                                definitionEt?.setText(definitionText)
                            }
                        }
                    }
                    addTranslateButton()
                } else {
                    addTranslateButton()
                }
                progressIndicator?.isVisible = false
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                showToast("Obtaining recommended words failed: ${e.message}")
                Log.e(TAG, "Obtaining recommended words failed: ${e.message}")
                resetWordRecommendation()
            }
        }
        searchJob?.invokeOnCompletion {
            progressIndicator?.isVisible = false
            searchJob = null
        }
    }

    private fun addTranslateButton() {
        recommendationLayout?.addButtonInLayout(
            context, "Translate with Lingvanex",
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        ) {
            progressIndicator?.isVisible = true
            coroutineScope?.launch {
                val result = floatingAddWordUtils?.getTranslatedWord()
                definitionEt?.setText(result)
                progressIndicator?.isVisible = false
            }
        }
    }

    private fun resetWordRecommendation() {
        searchJob?.cancel()
        recommendationLayout?.removeAllViews()
        searchWordBtn?.visibility = View.VISIBLE
    }

    override fun beforeCloseWindow(coroutineScope: CoroutineScope) {
        floatingAddWordUtils = null
        isAddWordWindowActive = false
    }

    override fun onItemClickListener(item: Word) {
        coroutineScope?.launch {
            floatingAddWordUtils?.updateShowForgotWord(item)
            FloatingInfoWordWindow(context, item).showWindow()
            closeWindow()
        }
    }

    override fun onItemLongClickListener(item: Word) {
        coroutineScope?.launch {
            if (categoryList != null) {
                EditWordActivity.startActivity(
                    context, item.wordId, categoryList!!
                )
            }
            closeWindow()
        }
    }

    companion object {
        private const val TAG = "FloatingAddWordWindow"
    }
}