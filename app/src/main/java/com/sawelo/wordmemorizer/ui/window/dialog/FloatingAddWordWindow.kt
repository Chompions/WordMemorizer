package com.sawelo.wordmemorizer.ui.window.dialog

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
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import com.sawelo.wordmemorizer.data.data_class.entity.Word
import com.sawelo.wordmemorizer.data.data_class.entity.WordInfo
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithCategories
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithInfo
import com.sawelo.wordmemorizer.data.preferences.settings.SettingsSwitch
import com.sawelo.wordmemorizer.databinding.WindowAddWordFloatingBinding
import com.sawelo.wordmemorizer.service.notification.NotificationFloatingBubbleService
import com.sawelo.wordmemorizer.ui.activity.EditWordActivity
import com.sawelo.wordmemorizer.ui.adapter.AddWordAdapter
import com.sawelo.wordmemorizer.ui.ui_util.FloatingDialogUtil
import com.sawelo.wordmemorizer.ui.ui_util.ViewUtils.addButtonInLayout
import com.sawelo.wordmemorizer.ui.ui_util.ViewUtils.addCategoryList
import com.sawelo.wordmemorizer.ui.ui_util.ViewUtils.checkCopyOrPaste
import com.sawelo.wordmemorizer.ui.ui_util.ViewUtils.showToast
import com.sawelo.wordmemorizer.ui.window.base.DialogWindow
import com.sawelo.wordmemorizer.util.callback.ItemWordAdapterListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*


abstract class FloatingAddWordWindow(
    private val context: Context,
    private val floatingDialogUtil: FloatingDialogUtil,
    private val selectedCategories: List<Category>? = null,
) : DialogWindow(context), ItemWordAdapterListener {

    private var binding: WindowAddWordFloatingBinding? = null
    private var searchJob: Job? = null
    private var adapter: AddWordAdapter? = null

    private val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private val isDrawBtnOn =
        sharedPreferences?.getBoolean(SettingsSwitch.DrawSwitch.switchKey, false) == true
    private val isTranslateBtnOn =
        sharedPreferences?.getBoolean(SettingsSwitch.TranslationSwitch.switchKey, false) == true

    override fun setViews(layoutInflater: LayoutInflater): ViewGroup {
        binding = WindowAddWordFloatingBinding.inflate(layoutInflater)
        return binding?.root?.apply {
            setOnTouchListener(this@FloatingAddWordWindow)
            setBackButtonListener(this@FloatingAddWordWindow)
        } ?: throw Exception("Binding cannot be null")
    }

    override fun beforeShowWindow() {
        if (isDrawBtnOn) binding?.dialogDrawWordBtn?.isVisible = true
        if (isTranslateBtnOn) binding?.dialogSearchWordTranslateBtn?.isVisible = true

        setAdapter()
        setDrawWindow()
        setWordsChangeListener()
        setCategoryList()
        setSearchButton()
        setActionButton()

        NotificationFloatingBubbleService.hideBubbleService(context)
        isWindowActive = true
    }

    override fun beforeCloseWindow() {
        floatingDialogUtil.destroyUtils()
        binding = null

        NotificationFloatingBubbleService.revealBubbleService(context)
        isWindowActive = false
    }

    private fun setAdapter() {
        adapter = AddWordAdapter(this)
        binding?.dialogSimilarWordRv?.adapter = adapter
        binding?.dialogSimilarWordRv?.layoutManager = LinearLayoutManager(context)
    }

    private fun setDrawWindow() {
        if (isDrawBtnOn) binding?.dialogDrawWordBtn?.setOnClickListener {
            FloatingDrawWordWindow(
                context, binding?.dialogAddWordEt?.text.toString(), this
            ) {
                binding?.dialogAddWordEt?.setText(it)
                searchWordRecommendations()
            }.showWindow()
            hideWindow()
        }
    }


    private fun TextInputLayout.setListener(inputType: FloatingDialogUtil.InputType) {
        editText?.setOnFocusChangeListener { _, isFocused ->
            resetWordRecommendation()
            floatingDialogUtil.focusedTextInput = inputType
            binding?.dialogSearchWordJishoBtn?.isEnabled = !editText?.text.isNullOrBlank()
            if (isTranslateBtnOn)
                binding?.dialogSearchWordTranslateBtn?.isEnabled = !editText?.text.isNullOrBlank()

            checkCopyOrPaste(isFocused)
        }

        editText?.doOnTextChanged { text, _, _, _ ->
            resetWordRecommendation()
            floatingDialogUtil.setWordFlow(inputType, text.toString())
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

        binding?.dialogAddWordIl?.setListener(FloatingDialogUtil.InputType.WORD_INPUT)
        binding?.dialogAddFuriganaIl?.setListener(FloatingDialogUtil.InputType.FURIGANA_INPUT)
        binding?.dialogAddDefinitionIl?.setListener(FloatingDialogUtil.InputType.DEFINITION_INPUT)

        windowCoroutineScope.launch {
            floatingDialogUtil.getAllWordsByTextFlow().collectLatest {
                binding?.dialogSimilarWordTv?.isVisible = it.isEmpty()
                adapter?.submitList(it)
            }
        }
    }

    private fun setCategoryList() {
        runBlocking {
            floatingDialogUtil.getAllCategories().let {
                binding?.dialogAddCategoryGroup?.addCategoryList(context, it)
            }
            selectedCategories?.forEach {
                binding?.dialogAddCategoryGroup?.check(it.categoryId)
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

    private fun showSearch(baseWord: Word) {
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
                val result = floatingDialogUtil.getTranslatedWord()
                if (result != null) showSearch(result)
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
                val result = floatingDialogUtil.getRecommendationsWords()
                if (result?.isEmpty() == true) {
                    resetWordRecommendation()
                    return@launch
                }
                binding?.dialogProgressIndicator?.isVisible = false
                binding?.dialogSearchWordJishoBtn?.visibility = View.INVISIBLE
                if (isTranslateBtnOn) binding?.dialogSearchWordTranslateBtn?.visibility =
                    View.INVISIBLE

                binding?.dialogRecommendationLayout?.addButtonInLayout(context, "Back") {
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
            windowCoroutineScope.launch {
                val wordWithCategories = WordWithCategories(
                    WordWithInfo(
                        Word(
                            wordText = binding?.dialogAddWordEt?.text.toString(),
                            furiganaText = binding?.dialogAddFuriganaEt?.text.toString(),
                            definitionText = binding?.dialogAddDefinitionEt?.text.toString(),
                        ),
                        WordInfo(
                            createdTimeMillis = System.currentTimeMillis(),
                        )
                    ),
                    floatingDialogUtil.getAllCategories().filter {
                        it.categoryId in (binding?.dialogAddCategoryGroup?.checkedButtonIds ?: emptyList())
                    }
                )
                val word = wordWithCategories.wordWithInfo.word
                when {
                    word.wordText.isBlank() -> context.showToast( "Word cannot be empty")
                    word.furiganaText.isBlank() -> context.showToast("Furigana cannot be empty")
                    word.definitionText.isBlank() -> context.showToast("Definition cannot be empty")
                    wordWithCategories.categories.isEmpty() -> context.showToast("Category cannot be empty")
                    else -> {
                        windowCoroutineScope.launch {
                            floatingDialogUtil.addWord(wordWithCategories)
                            closeWindow()
                        }
                    }
                }
            }
        }
        binding?.dialogCancelBtn?.setOnClickListener {
            closeWindow()
        }
    }

    override fun onItemClickListener(item: WordWithInfo) {
        windowCoroutineScope.launch {
            floatingDialogUtil.updateShowForgotWord(item.word)
            FloatingInfoWordWindow(context, item.word).showWindow()
            closeWindow()
        }
    }

    override fun onItemLongClickListener(item: WordWithInfo) {
        windowCoroutineScope.launch {
            EditWordActivity.startActivity(
                context, item.word.wordId,
            )
            closeWindow()
        }
    }

    companion object {
        private const val TAG = "FloatingAddWordWindow"
        var isWindowActive = false
            private set
    }

}