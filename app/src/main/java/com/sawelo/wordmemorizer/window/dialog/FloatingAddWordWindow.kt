package com.sawelo.wordmemorizer.window.dialog

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.LocaleList
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import com.sawelo.wordmemorizer.activity.EditWordActivity
import com.sawelo.wordmemorizer.adapter.AddWordAdapter
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.BaseWord
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import com.sawelo.wordmemorizer.databinding.WindowAddWordFloatingBinding
import com.sawelo.wordmemorizer.service.NotificationFloatingBubbleService
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_DRAW_CHARACTER_KEY
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_OFFLINE_TRANSLATION_KEY
import com.sawelo.wordmemorizer.util.FloatingDialogUtils
import com.sawelo.wordmemorizer.util.ViewUtils.addButtonInLayout
import com.sawelo.wordmemorizer.util.ViewUtils.addCategoryList
import com.sawelo.wordmemorizer.util.ViewUtils.checkCopyOrPaste
import com.sawelo.wordmemorizer.util.ViewUtils.showToast
import com.sawelo.wordmemorizer.util.callback.ItemWordAdapterListener
import com.sawelo.wordmemorizer.window.DialogWindow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.*


class FloatingAddWordWindow(
    private val context: Context,
    private val wordRepository: WordRepository,
    private val currentCategory: Category?,
) : DialogWindow(context), ItemWordAdapterListener {

    private var binding: WindowAddWordFloatingBinding? = null
    private var searchJob: Job? = null
    private var adapter: AddWordAdapter? = null
    private var floatingDialogUtils: FloatingDialogUtils? = null
    private var categoryList: List<Category>? = null

    private val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private val isDrawBtnOn =
        sharedPreferences?.getBoolean(PREFERENCE_DRAW_CHARACTER_KEY, false) == true
    private val isTranslateBtnOn =
        sharedPreferences?.getBoolean(PREFERENCE_OFFLINE_TRANSLATION_KEY, false) == true

    override fun setViews(layoutInflater: LayoutInflater): ViewGroup {
        binding = WindowAddWordFloatingBinding.inflate(layoutInflater)
        return binding?.root?.apply {
            setOnTouchListener(this@FloatingAddWordWindow)
            setBackButtonListener(this@FloatingAddWordWindow)
        } ?: throw Exception("Binding cannot be null")
    }

    override fun beforeShowWindow() {
        floatingDialogUtils = FloatingDialogUtils(wordRepository)

        if (isDrawBtnOn) binding?.dialogDrawWordBtn?.isVisible = true
        if (isTranslateBtnOn) binding?.dialogSearchWordTranslateBtn?.isVisible = true

        setAdapter()
        setDrawWindow()
        setWordsChangeListener()
        setCategoryList()
        setSearchButton()
        setActionButton()

        NotificationFloatingBubbleService.hideBubbleService(context)
        isAddWordWindowActive = true
    }

    override fun beforeCloseWindow() {
        floatingDialogUtils?.destroyUtils()
        floatingDialogUtils = null
        binding = null

        NotificationFloatingBubbleService.revealBubbleService(context)
        isAddWordWindowActive = false
    }

    private fun setAdapter() {
        adapter = AddWordAdapter(this)
        binding?.dialogSimilarWordRv?.adapter = adapter
        binding?.dialogSimilarWordRv?.layoutManager = LinearLayoutManager(context)
    }

    private fun setDrawWindow() {
        val currentWordText = binding?.dialogAddWordEt?.text.toString()
        if (isDrawBtnOn) binding?.dialogDrawWordBtn?.setOnClickListener {
            FloatingDrawWordWindow(
                context, currentWordText, this
            ) {
                binding?.dialogAddWordEt?.setText(it)
            }.showWindow()
            hideWindow()
        }
    }


    private fun TextInputLayout.setListener(inputType: FloatingDialogUtils.InputType) {
        editText?.setOnFocusChangeListener { _, isFocused ->
            resetWordRecommendation()
            floatingDialogUtils?.focusedTextInput = inputType
            binding?.dialogSearchWordJishoBtn?.isEnabled = !editText?.text.isNullOrBlank()
            if (isTranslateBtnOn)
                binding?.dialogSearchWordTranslateBtn?.isEnabled = !editText?.text.isNullOrBlank()

            checkCopyOrPaste(isFocused)
        }

        editText?.doOnTextChanged { text, _, _, _ ->
            resetWordRecommendation()
            floatingDialogUtils?.setWordFlow(inputType, text.toString())
            binding?.dialogSearchWordJishoBtn?.isEnabled = !editText?.text.isNullOrBlank()
            if (isTranslateBtnOn)
                binding?.dialogSearchWordTranslateBtn?.isEnabled = !editText?.text.isNullOrBlank()

            checkCopyOrPaste(true)
        }
    }

    private fun setWordsChangeListener() {
        val inputMethodManager =
            context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        binding?.dialogAddWordEt?.apply {
            setRawInputType(InputType.TYPE_CLASS_TEXT)
            imeHintLocales = LocaleList(Locale.JAPANESE)
            setOnEditorActionListener { _, _, _ ->
                binding?.dialogAddFuriganaEt?.requestFocus()
                true
            }
        }
        binding?.dialogAddFuriganaEt?.apply {
            setRawInputType(InputType.TYPE_CLASS_TEXT)
            imeHintLocales = LocaleList(Locale.JAPANESE)
            setOnEditorActionListener { _, _, _ ->
                binding?.dialogAddDefinitionEt?.requestFocus()
                true
            }
        }
        binding?.dialogAddDefinitionEt?.apply {
            setRawInputType(InputType.TYPE_CLASS_TEXT)
            imeHintLocales = LocaleList(Locale.ENGLISH)
            setOnEditorActionListener { view, _, _ ->
                binding?.dialogAddDefinitionEt?.clearFocus()
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                true
            }
        }

        binding?.dialogAddWordIl?.setListener(FloatingDialogUtils.InputType.WORD_INPUT)
        binding?.dialogAddFuriganaIl?.setListener(FloatingDialogUtils.InputType.FURIGANA_INPUT)
        binding?.dialogAddDefinitionIl?.setListener(FloatingDialogUtils.InputType.DEFINITION_INPUT)

        windowCoroutineScope.launch {
            floatingDialogUtils?.getAllWordsByTextFlow()?.collectLatest {
                binding?.dialogSimilarWordTv?.isVisible = it.isEmpty()
                adapter?.submitList(it)
            }
        }
    }

    private fun setCategoryList() {
        runBlocking {
            categoryList = floatingDialogUtils?.getAllCategories()
            if (categoryList != null) {
                binding?.dialogAddCategoryGroup?.addCategoryList(context, categoryList!!)
            }
            if (currentCategory != null) {
                binding?.dialogAddCategoryGroup?.check(currentCategory.categoryId)
            }
        }
    }

    private fun prepareSearch() {
        searchJob?.cancel()
        binding?.dialogRecommendationLayout?.removeAllViews()
        binding?.dialogSearchWordJishoBtn?.isEnabled = false
        if (isTranslateBtnOn) binding?.dialogSearchWordTranslateBtn?.isEnabled = false
        binding?.dialogProgressIndicator?.isVisible = true
    }

    private fun showSearch(baseWord: BaseWord) {
        binding?.dialogAddWordEt?.setText(baseWord.wordText)
        binding?.dialogAddFuriganaEt?.setText(baseWord.furiganaText)
        binding?.dialogAddDefinitionEt?.setText(baseWord.definitionText)
    }

    private fun afterSearch() {
        binding?.dialogSearchWordJishoBtn?.isEnabled = true
        if (isTranslateBtnOn) binding?.dialogSearchWordTranslateBtn?.isEnabled = true

        binding?.dialogProgressIndicator?.isVisible = false
        searchJob = null
    }

    private fun resetWordRecommendation() {
        searchJob?.cancel()
        binding?.dialogRecommendationLayout?.removeAllViews()
        binding?.dialogSearchWordJishoBtn?.visibility = View.VISIBLE
        if (isTranslateBtnOn) binding?.dialogSearchWordTranslateBtn?.visibility = View.VISIBLE
        afterSearch()
    }

    private fun searchWordTranslate() {
        prepareSearch()
        searchJob = windowCoroutineScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { floatingDialogUtils?.getTranslatedWord() }
                binding?.dialogProgressIndicator?.isVisible = false
                result?.let { showSearch(it) }
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                context.showToast("Obtaining translated word failed: ${e.message}")
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
        searchJob = windowCoroutineScope.launch {
            try {
                val result = floatingDialogUtils?.getRecommendationsWords()
                if (result?.isEmpty() == true) {
                    resetWordRecommendation()
                    return@launch
                }
                binding?.dialogProgressIndicator?.isVisible = false
                binding?.dialogSearchWordJishoBtn?.visibility = View.INVISIBLE
                if (isTranslateBtnOn) binding?.dialogSearchWordTranslateBtn?.visibility = View.INVISIBLE

                binding?.dialogRecommendationLayout?.addButtonInLayout(context, "Reset") {
                    resetWordRecommendation()
                }
                result?.forEach { baseWord ->
                    val fixedBaseWord = baseWord.copy(
                        wordText = baseWord.wordText.ifBlank { baseWord.furiganaText }
                    )
                    binding?.dialogRecommendationLayout?.addButtonInLayout(
                        context, fixedBaseWord.wordText
                    ) { showSearch(fixedBaseWord) }
                }
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                context.showToast("Obtaining recommended words failed: ${e.message}")
                Log.e(TAG, "Obtaining recommended words failed: ${e.message}")
                resetWordRecommendation()
            }
        }
        searchJob?.invokeOnCompletion {
            afterSearch()
        }
    }

    private fun setSearchButton() {
        binding?.dialogSearchWordJishoBtn?.setOnClickListener {
            searchWordRecommendations()
        }

        if (isTranslateBtnOn) binding?.dialogSearchWordTranslateBtn?.setOnClickListener {
            searchWordTranslate()
        }
    }

    private fun setActionButton() {
        binding?.dialogAddWordBtn?.setOnClickListener {
            val wordWithCategories = WordWithCategories(
                Word(
                    wordText = binding?.dialogAddWordEt?.text.toString(),
                    furiganaText = binding?.dialogAddFuriganaEt?.text.toString(),
                    definitionText = binding?.dialogAddDefinitionEt?.text.toString(),
                    createdTimeMillis = System.currentTimeMillis(),
                ),
                categoryList!!.filter {
                    it.categoryId in (binding?.dialogAddCategoryGroup?.checkedButtonIds ?: emptyList())
                }
            )
            when {
                wordWithCategories.word.wordText.isBlank() -> context.showToast( "Word cannot be empty")
                wordWithCategories.word.furiganaText.isBlank() -> context.showToast("Furigana cannot be empty")
                wordWithCategories.word.definitionText.isBlank() -> context.showToast("Definition cannot be empty")
                else -> {
                    windowCoroutineScope.launch {
                        floatingDialogUtils?.addWord(wordWithCategories)
                        closeWindow()
                    }
                }
            }
        }
        binding?.dialogCancelBtn?.setOnClickListener {
            closeWindow()
        }
    }

    override fun onItemClickListener(item: Word) {
        windowCoroutineScope.launch {
            floatingDialogUtils?.updateShowForgotWord(item)
            FloatingInfoWordWindow(context, wordRepository, item).showWindow()
            closeWindow()
        }
    }

    override fun onItemLongClickListener(item: Word) {
        windowCoroutineScope.launch {
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
        var isAddWordWindowActive = false
            private set
    }

}