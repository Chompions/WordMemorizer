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
import androidx.preference.PreferenceManager
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
import com.sawelo.wordmemorizer.data.data_class.BaseWord
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import com.sawelo.wordmemorizer.util.Constants
import com.sawelo.wordmemorizer.util.Constants.isAddWordWindowActive
import com.sawelo.wordmemorizer.util.FloatingUtils
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
    private val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

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
    private var searchWordJishoBtn: Button? = null
    private var searchWordTranslateBtn: Button? = null

    private var addCategoryGroup: MaterialButtonToggleGroup? = null
    private var addBtn: Button? = null
    private var cancelBtn: Button? = null

    private var coroutineScope: CoroutineScope? = null
    private var searchJob: Job? = null
    private var adapter: AddWordAdapter? = null
    private var floatingUtils: FloatingUtils? = null
    private var categoryList: List<Category>? = null

    override fun setViews(parent: ViewGroup) {
        wordEt = parent.findViewById(R.id.dialog_addWord_et)
        furiganaEt = parent.findViewById(R.id.dialog_addFurigana_et)
        definitionEt = parent.findViewById(R.id.dialog_addDefinition_et)

        wordIl = parent.findViewById(R.id.dialog_addWord_il)
        furiganaIl = parent.findViewById(R.id.dialog_addFurigana_il)
        definitionIl = parent.findViewById(R.id.dialog_addDefinition_il)

        similarWordRv = parent.findViewById(R.id.dialog_similarWord_rv)
        similarWordTv = parent.findViewById(R.id.dialog_similarWord_tv)

        progressIndicator = parent.findViewById(R.id.dialog_progressIndicator)
        recommendationLayout = parent.findViewById(R.id.dialog_recommendationLayout)
        searchWordJishoBtn = parent.findViewById(R.id.dialog_searchWordJisho_btn)

        addCategoryGroup = parent.findViewById(R.id.dialog_addCategory_group)
        addBtn = parent.findViewById(R.id.dialog_addWord_btn)
        cancelBtn = parent.findViewById(R.id.dialog_cancel_btn)

        val currentDrawPreference =
            sharedPreferences?.getBoolean(Constants.PREFERENCE_DRAW_CHARACTER_KEY, false)
        if (currentDrawPreference == true) {
            drawWordBtn = parent.findViewById(R.id.dialog_drawWord_btn)
        }

        val currentTranslatePreference =
            sharedPreferences?.getBoolean(Constants.PREFERENCE_OFFLINE_TRANSLATION_KEY, false)
        if (currentTranslatePreference == true) {
            searchWordTranslateBtn = parent.findViewById(R.id.dialog_searchWordTranslate_btn)
        }
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
        searchWordJishoBtn = null
        searchWordTranslateBtn = null

        addCategoryGroup = null
        addBtn = null
        cancelBtn = null
    }

    override fun beforeShowWindow(coroutineScope: CoroutineScope) {
        this.coroutineScope = coroutineScope

        /**
         * All functionality that involves repository should only be managed through Utils
         */
        floatingUtils = FloatingUtils(wordRepository)

        setAdapter()
        setDrawWindow()
        setWordsChangeListener()
        setCategoryList()
        setSearchButton()
        setActionButton()

        isAddWordWindowActive = true
    }

    private fun setAdapter() {
        adapter = AddWordAdapter(this)
        similarWordRv?.adapter = adapter
        similarWordRv?.layoutManager = LinearLayoutManager(context)
    }

    private fun setDrawWindow() {
        drawWordBtn?.isVisible = true
        drawWordBtn?.setOnClickListener {
            FloatingDrawWordWindow(
                context, wordEt?.text.toString(), this
            ) {
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

    private fun TextInputLayout.setListener(inputType: FloatingUtils.InputType) {
        editText?.setOnFocusChangeListener { _, isFocused ->
            resetWordRecommendation()
            floatingUtils?.focusedTextInput = inputType
            searchWordJishoBtn?.isEnabled = !editText?.text.isNullOrBlank()
            searchWordTranslateBtn?.isEnabled = !editText?.text.isNullOrBlank()

            checkCopyOrPaste()
            isEndIconVisible = isFocused
        }

        editText?.doOnTextChanged { text, _, _, _ ->
            resetWordRecommendation()
            floatingUtils?.setWordFlow(inputType, text.toString())
            searchWordJishoBtn?.isEnabled = !editText?.text.isNullOrBlank()
            searchWordTranslateBtn?.isEnabled = !editText?.text.isNullOrBlank()

            checkCopyOrPaste()
        }
    }

    private fun setWordsChangeListener() {
        wordIl?.setListener(FloatingUtils.InputType.WORD_INPUT)
        furiganaIl?.setListener(FloatingUtils.InputType.FURIGANA_INPUT)
        definitionIl?.setListener(FloatingUtils.InputType.DEFINITION_INPUT)

        coroutineScope?.launch {
            floatingUtils?.getAllWordsByTextFlow()?.collectLatest {
                similarWordTv?.isVisible = it.isEmpty()
                adapter?.submitList(it)
            }
        }
    }

    private fun setCategoryList() {
        runBlocking {
            categoryList = floatingUtils?.getAllCategories()
            if (categoryList != null) {
                addCategoryGroup?.addCategoryList(context, categoryList!!)
            }
            if (currentCategory != null) {
                addCategoryGroup?.check(currentCategory.categoryId)
            }
        }
    }

    private fun setSearchButton() {
        searchWordJishoBtn?.setOnClickListener {
            searchWordRecommendations()
        }

        searchWordTranslateBtn?.isVisible = true
        searchWordTranslateBtn?.setOnClickListener {
            searchWordTranslate()
        }
    }

    private fun searchWordTranslate() {
        prepareSearch()
        searchJob = coroutineScope?.launch {
            try {
                val result = withContext(Dispatchers.IO) {floatingUtils?.getTranslatedWord()}
                progressIndicator?.isVisible = false
                result?.let { showSearch(it) }
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                showToast("Obtaining translated word failed: ${e.message}")
                Log.e(TAG, "Obtaining translated word failed: ${e.message}")
                resetWordRecommendation()
            }
        }
        searchJob?.invokeOnCompletion {
            afterSearch()
        }
    }

    private fun searchWordRecommendations() {
        prepareSearch()
        searchJob = coroutineScope?.launch {
            try {
                val result = floatingUtils?.getRecommendationsWords()
                progressIndicator?.isVisible = false
                searchWordJishoBtn?.visibility = View.INVISIBLE
                searchWordTranslateBtn?.visibility = View.INVISIBLE

                result?.forEach { baseWord ->
                    recommendationLayout?.addButtonInLayout(context, baseWord.wordText) {
                        showSearch(baseWord)
                    }
                }
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                showToast("Obtaining recommended words failed: ${e.message}")
                Log.e(TAG, "Obtaining recommended words failed: ${e.message}")
                resetWordRecommendation()
            }
        }
        searchJob?.invokeOnCompletion {
            afterSearch()
        }
    }

    private fun prepareSearch() {
        searchJob?.cancel()
        recommendationLayout?.removeAllViews()
        searchWordJishoBtn?.isEnabled = false
        searchWordTranslateBtn?.isEnabled = false

        progressIndicator?.isVisible = true
    }

    private fun showSearch(baseWord: BaseWord) {
        wordEt?.setText(baseWord.wordText)
        furiganaEt?.setText(baseWord.furiganaText)
        definitionEt?.setText(baseWord.definitionText)
    }

    private fun afterSearch() {
        searchWordJishoBtn?.isEnabled = true
        searchWordTranslateBtn?.isEnabled = true

        progressIndicator?.isVisible = false
        searchJob = null
    }

    private fun resetWordRecommendation() {
        searchJob?.cancel()
        recommendationLayout?.removeAllViews()
        searchWordJishoBtn?.visibility = View.VISIBLE
        searchWordTranslateBtn?.visibility = View.VISIBLE
        afterSearch()
    }

    private fun setActionButton() {
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
                        floatingUtils?.addWord(wordWithCategories)
                        closeWindow()
                    }
                }
            }
        }
        cancelBtn?.setOnClickListener {
            closeWindow()
        }
    }

    override fun setAdditionalParams(params: WindowManager.LayoutParams?) {}

    override fun beforeCloseWindow(coroutineScope: CoroutineScope) {
        floatingUtils?.destroyUtils()
        floatingUtils = null
        isAddWordWindowActive = false
    }

    override fun onItemClickListener(item: Word) {
        coroutineScope?.launch {
            floatingUtils?.updateShowForgotWord(item)
            FloatingInfoWordWindow(context, wordRepository, item).showWindow()
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