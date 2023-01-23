package com.sawelo.wordmemorizer.fragment.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.adapter.AddWordAdapter
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import com.sawelo.wordmemorizer.util.WordUtils.isAll
import com.sawelo.wordmemorizer.util.callback.ItemWordAdapterCallback
import com.sawelo.wordmemorizer.viewmodel.AddWordViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddWordDialogFragment : DialogFragment(), ItemWordAdapterCallback {
    private val viewModel: AddWordViewModel by activityViewModels()
    private lateinit var addWordDialog: AlertDialog
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val view = layoutInflater.inflate(R.layout.dialog_add_word, null)
            val builder = MaterialAlertDialogBuilder(activity).setView(view)
            addWordDialog = builder.create()

            wordEt = view.findViewById(R.id.dialog_addWord_et)
            furiganaEt = view.findViewById(R.id.dialog_addFurigana_et)
            definitionEt = view.findViewById(R.id.dialog_addDefinition_et)
            similarWordRv = view.findViewById(R.id.dialog_similarWord_rv)
            similarWordTv = view.findViewById(R.id.dialog_similarWord_tv)

            progressIndicator = view.findViewById(R.id.dialog_addCategory_progressIndicator)
            recommendationLayout = view.findViewById(R.id.dialog_addCategory_recommendationLayout)
            addCategoryGroup = view.findViewById(R.id.dialog_addCategory_group)
            addBtn = view.findViewById(R.id.dialog_addWord_btn)

            adapter = AddWordAdapter(this)
            similarWordRv?.adapter = adapter
            similarWordRv?.layoutManager = LinearLayoutManager(activity)

            getWordsOnTextChanged(activity)
            getCategoryList(activity)
            setAddButton()

            addWordDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onItemClickListener(item: Word) {
        viewModel.updateShowForgotWord(item)
        addWordDialog.dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.clearFlow()
    }

    private fun addRecommendationButton(
        context: Context,
        wordText: String,
        furiganaText: String,
        definitionText: String
    ) {
        val recommendationButton = MaterialButton(
            context, null,
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

    private fun getWordsOnTextChanged(context: Context) {
        wordEt?.doOnTextChanged { text, _, _, _ ->
            viewModel.wordTextFlow.value = text.toString()
        }
        furiganaEt?.doOnTextChanged { text, _, _, _ ->
            viewModel.furiganaTextFlow.value = text.toString()
        }
        definitionEt?.doOnTextChanged { text, _, _, _ ->
            viewModel.definitionTextFlow.value = text.toString()
        }

        lifecycleScope.launch {
            viewModel.progressIndicatorShowFlow.collectLatest {
                progressIndicator?.isVisible = it
            }
        }

        lifecycleScope.launch {
            viewModel.getAllWordsByTextFlow().collectLatest {
                similarWordTv?.isVisible = it.isEmpty()
                adapter?.submitList(it)
            }
        }

        lifecycleScope.launch {
            try {
                viewModel.getRecommendationWordsFlow()
                    .collectLatest {
                        recommendationLayout?.removeAllViews()
                        it.forEach { data ->
                            val wordText = data.japanese.first().word
                            val furiganaText = data.japanese.first().reading
                            val definitionText =
                                data.senses.first().englishDefinitions.joinToString(" / ")
                            addRecommendationButton(context, wordText, furiganaText, definitionText)
                        }
                        progressIndicator?.hide()
                    }
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                showToast("Obtaining recommended words failed: ${e.message}")
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getCategoryList(context: Context) {
        categoryList = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelableArrayList(ADD_DIALOG_FRAGMENT_ARGS, Category::class.java)
        } else {
            arguments?.getParcelableArrayList(ADD_DIALOG_FRAGMENT_ARGS)
        }

        if (categoryList != null) {
            for (category in categoryList!!) {
                if (!category.isAll()) {
                    val button = MaterialButton(
                        context, null,
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

    private fun setAddButton() {
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
                    viewModel.addWord(wordWithCategories)
                    addWordDialog.dismiss()
                }
            }
        }
    }

    private fun showToast(text: String?) {
        Toast
            .makeText(activity, text, Toast.LENGTH_SHORT)
            .show()
    }

    companion object {
        fun newInstance(categoryList: List<Category>): AddWordDialogFragment {
            val dialogFragment = AddWordDialogFragment()
            dialogFragment.arguments = Bundle().apply {
                putParcelableArrayList(ADD_DIALOG_FRAGMENT_ARGS, ArrayList(categoryList))
            }
            return dialogFragment
        }

        private const val ADD_DIALOG_FRAGMENT_ARGS = "ADD_WORD_DIALOG_FRAGMENT_ARGS"
    }

}