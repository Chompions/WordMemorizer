package com.sawelo.wordmemorizer.util

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.graphics.PixelFormat
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.adapter.AddWordAdapter
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import com.sawelo.wordmemorizer.util.NotificationUtils.NOTIFICATION_START_ACTION
import com.sawelo.wordmemorizer.util.NotificationUtils.NOTIFICATION_STOP_ACTION
import com.sawelo.wordmemorizer.util.WordUtils.isAll
import com.sawelo.wordmemorizer.util.callback.ItemWordAdapterCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class FloatingBubbleReceiver : BroadcastReceiver(), ItemWordAdapterCallback {
    @Inject
    lateinit var wordRepository: WordRepository

    private var floatingBubbleUtil: FloatingBubbleUtil? = null
    private var floatingBubbleDialogView: ViewGroup? = null
    private var floatingBubbleInfoDialogView: ViewGroup? = null

    private var layoutInflater: LayoutInflater? = null
    private var windowManager: WindowManager? = null
    private var params: WindowManager.LayoutParams? = null

    private var isShown: Boolean = false
    private var categoryList: List<Category>? = null
    private var adapter: AddWordAdapter? = null

    private var wordEt: EditText? = null
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
    private var contextThemeWrapper: Context? = null

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NOTIFICATION_STOP_ACTION -> {
                removeFloatingMenu()
            }
            NOTIFICATION_START_ACTION -> {
                if (!isShown) {
                    addFloatingMenu(context)
                } else {
                    removeFloatingMenu()
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun addFloatingMenu(context: Context) {
        floatingBubbleUtil = FloatingBubbleUtil(wordRepository)
        coroutineScope = CoroutineScope(Dispatchers.Main)
        contextThemeWrapper = ContextThemeWrapper(context, R.style.Theme_WordMemorizer)

        layoutInflater = contextThemeWrapper?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingBubbleDialogView =
            layoutInflater?.inflate(R.layout.window_add_word_floating, null) as ViewGroup

        wordEt = floatingBubbleDialogView?.findViewById(R.id.dialog_addWord_et)
        furiganaEt = floatingBubbleDialogView?.findViewById(R.id.dialog_addFurigana_et)
        definitionEt = floatingBubbleDialogView?.findViewById(R.id.dialog_addDefinition_et)
        similarWordRv = floatingBubbleDialogView?.findViewById(R.id.dialog_similarWord_rv)
        similarWordTv = floatingBubbleDialogView?.findViewById(R.id.dialog_similarWord_tv)

        progressIndicator =
            floatingBubbleDialogView?.findViewById(R.id.dialog_addCategory_progressIndicator)
        recommendationLayout =
            floatingBubbleDialogView?.findViewById(R.id.dialog_addCategory_recommendationLayout)
        addCategoryGroup = floatingBubbleDialogView?.findViewById(R.id.dialog_addCategory_group)
        addBtn = floatingBubbleDialogView?.findViewById(R.id.dialog_addWord_btn)
        cancelBtn = floatingBubbleDialogView?.findViewById(R.id.dialog_cancel_btn)

        adapter = AddWordAdapter(this)
        similarWordRv?.adapter = adapter
        similarWordRv?.layoutManager = LinearLayoutManager(contextThemeWrapper)

        getWordsOnTextChanged()
        getCategoryList()
        setButton()

        windowManager = contextThemeWrapper?.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )
        if (contextThemeWrapper != null) {
            params?.width = contextThemeWrapper!!.resources.displayMetrics.widthPixels - 100
        }
        params?.gravity = Gravity.CENTER
        params?.windowAnimations = android.R.style.Animation_Dialog

        windowManager?.addView(floatingBubbleDialogView, params)

        isShown = true
    }

    private fun removeFloatingMenu() {
        if (floatingBubbleDialogView != null) {
            windowManager?.removeView(floatingBubbleDialogView)
            floatingBubbleDialogView = null
        }
        if (floatingBubbleInfoDialogView != null) {
            windowManager?.removeView(floatingBubbleInfoDialogView)
            floatingBubbleInfoDialogView = null
        }
        coroutineScope = null
        floatingBubbleUtil = null
        contextThemeWrapper = null

        isShown = false
    }

    override fun onItemClickListener(item: Word) {
        coroutineScope?.launch {
            floatingBubbleUtil?.updateShowForgotWord(item)
            addInfoWindow(item)
        }
    }

    @SuppressLint("InflateParams")
    private fun addInfoWindow(item: Word) {
        floatingBubbleInfoDialogView =
            layoutInflater?.inflate(R.layout.window_add_word_info_floating, null) as ViewGroup
        val wordTv: TextView? = floatingBubbleInfoDialogView?.findViewById(R.id.info_wordText)
        val furiganaTv: TextView? =
            floatingBubbleInfoDialogView?.findViewById(R.id.info_furiganaText)
        val definitionTv: TextView? =
            floatingBubbleInfoDialogView?.findViewById(R.id.info_definitionText)
        val okBtn: Button? = floatingBubbleInfoDialogView?.findViewById(R.id.info_ok)

        wordTv?.text = item.wordText
        furiganaTv?.text = item.furiganaText
        definitionTv?.text = item.definitionText
        okBtn?.setOnClickListener {
            removeFloatingMenu()
        }

        if (contextThemeWrapper != null) {
            params?.width = contextThemeWrapper!!.resources.displayMetrics.widthPixels - 200
        }

        windowManager?.removeView(floatingBubbleDialogView)
        floatingBubbleDialogView = null
        windowManager?.addView(floatingBubbleInfoDialogView, params)
    }

    private fun addRecommendationButton(
        wordText: String,
        furiganaText: String,
        definitionText: String
    ) {
        if (contextThemeWrapper != null) {
            val recommendationButton = MaterialButton(
                contextThemeWrapper!!, null,
                com.google.android.material.R.attr.materialIconButtonFilledTonalStyle
            ).apply {
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 4, 0)
                layoutParams = params
                text = wordText
                setOnClickListener {
                    wordEt?.setText(wordText)
                    furiganaEt?.setText(furiganaText)
                    definitionEt?.setText(definitionText)
                }
            }
            recommendationLayout?.addView(recommendationButton)
        }
    }

    private fun getWordsOnTextChanged() {
        wordEt?.doOnTextChanged { text, _, _, _ ->
            floatingBubbleUtil?.wordTextFlow?.value = text.toString()
        }
        furiganaEt?.doOnTextChanged { text, _, _, _ ->
            floatingBubbleUtil?.furiganaTextFlow?.value = text.toString()
        }
        definitionEt?.doOnTextChanged { text, _, _, _ ->
            floatingBubbleUtil?.definitionTextFlow?.value = text.toString()
        }

        coroutineScope?.launch {
            floatingBubbleUtil?.progressIndicatorShowFlow?.collectLatest {
                progressIndicator?.isVisible = it
            }
        }

        coroutineScope?.launch {
            floatingBubbleUtil?.getAllWordsByTextFlow()?.collectLatest {
                similarWordTv?.isVisible = it.isEmpty()
                adapter?.submitList(it)
            }
        }

        coroutineScope?.launch {
            floatingBubbleUtil?.getRecommendationWordsFlow()
                ?.collectLatest {
                    recommendationLayout?.removeAllViews()
                    it.forEach { data ->
                        val wordText = data.japanese.first().word
                        val furiganaText = data.japanese.first().reading
                        val definitionText =
                            data.senses.first().englishDefinitions.joinToString(" / ")
                        addRecommendationButton(wordText, furiganaText, definitionText)
                    }
                    progressIndicator?.hide()
                }
        }
    }

    @Suppress("DEPRECATION")
    private fun getCategoryList() {
        runBlocking {
            categoryList = floatingBubbleUtil?.getAllCategories()

            if (categoryList != null && contextThemeWrapper != null) {
                for (category in categoryList!!) {
                    if (!category.isAll()) {
                        val button = MaterialButton(
                            contextThemeWrapper!!, null,
                            com.google.android.material.R.attr.materialButtonOutlinedStyle
                        ).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            text = category.categoryName
                            id = category.categoryId
                        }
                        (addCategoryGroup as ViewGroup).addView(button)
                    }
                }
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
                        floatingBubbleUtil?.addWord(wordWithCategories)
                        removeFloatingMenu()
                    }
                }
            }
        }
        cancelBtn?.setOnClickListener {
            removeFloatingMenu()
        }
    }

    private fun showToast(text: String) {
        Toast
            .makeText(contextThemeWrapper, text, Toast.LENGTH_SHORT)
            .show()
    }
}