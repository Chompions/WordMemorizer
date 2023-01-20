package com.sawelo.wordmemorizer.fragment.dialog

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.adapter.AddWordAdapter
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.util.callback.ItemWordAdapterCallback
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class UpdateWordDialogFragment : DialogFragment(), ItemWordAdapterCallback {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var updateWordDialog: AlertDialog
    private var currentWord: Word? = null
    private var categoryList: List<Category>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val view = layoutInflater.inflate(R.layout.dialog_update_word, null)
            val builder = MaterialAlertDialogBuilder(activity).setView(view)
            updateWordDialog = builder.create()

            val wordEt = view.findViewById<EditText>(R.id.dialog_updateWord_et)
            val furiganaEt = view.findViewById<EditText>(R.id.dialog_updateFurigana_et)
            val definitionEt = view.findViewById<EditText>(R.id.dialog_updateDefinition_et)
            val similarWordRv = view.findViewById<RecyclerView>(R.id.dialog_similarWord_rv)
            val similarWordTv = view.findViewById<TextView>(R.id.dialog_similarWord_tv)
            val addCategoryGroup =
                view.findViewById<MaterialButtonToggleGroup>(R.id.dialog_updateCategory_group)
            val deleteBtn: Button = view.findViewById(R.id.dialog_deleteWord_btn)
            val updateBtn: Button = view.findViewById(R.id.dialog_updateWord_btn)

            val adapter = AddWordAdapter(this)
            similarWordRv.adapter = adapter
            similarWordRv.layoutManager = LinearLayoutManager(activity)

            wordEt.doOnTextChanged { text, _, _, _ ->
                lifecycleScope.launch {
                    val wordString = text.toString()
//                    if (Wanakana.isJapanese(wordString)) {
//                        val tokens: List<Token> = viewModel.tokenizer.tokenize(wordString)
//                        val hiraganaTokens = tokens.map {
//                            Wanakana.toHiragana(it.reading)
//                        }
//                        furiganaEt.setText(hiraganaTokens.joinToString())
//                    }
//                    viewModel.getAllWordsByWord(wordString) {
//                        if (wordString.isNotBlank() && it.isNotEmpty()) {
//                            similarWordTv.isVisible = false
//                            adapter.submitList(it)
//                        } else {
//                            similarWordTv.isVisible = true
//                            adapter.submitList(emptyList())
//                        }
//                    }
                }
            }

            getParcelable()

            if (currentWord != null && categoryList != null) {

                wordEt.setText(currentWord!!.wordText)
                furiganaEt.setText(currentWord!!.furiganaText)
                definitionEt.setText(currentWord!!.definitionText)

//                for (category in categoryList!!) {
//                    if (!category.isAll()) {
//                        val button = MaterialButton(
//                            activity, null,
//                            com.google.android.material.R.attr.materialButtonOutlinedStyle
//                        ).apply {
//                            layoutParams = ViewGroup.LayoutParams(
//                                ViewGroup.LayoutParams.WRAP_CONTENT,
//                                ViewGroup.LayoutParams.WRAP_CONTENT
//                            )
//                            text = category.categoryName
//                            id = category.id
//                            println("EOFJEFUJ ${category}")
//                            println("EOFJEFUJ ${currentWord!!.categoryList}")
//                            println("EOFJEFUJ ${category in currentWord!!.categoryList}")
//                        }
//                        (addCategoryGroup as ViewGroup).addView(button)
//
//                        if (category in currentWord!!.categoryList) {
//                            addCategoryGroup.check(category.id)
//                        }
//                    }
//                }

//                deleteBtn.setOnClickListener {
//                    viewModel.deleteWord(currentWord!!)
//                    showToast("You deleted ${currentWord!!.wordText}")
//                    updateWordDialog.dismiss()
//                }

                updateBtn.setOnClickListener {
                    val word = currentWord!!.copy(
                        wordText = wordEt.text.toString(),
                        furiganaText = furiganaEt.text.toString(),
                        definitionText = definitionEt.text.toString(),
                        createdTimeMillis = System.currentTimeMillis(),
                    )

                    println("SEUGHISEUH $word")

//                    categoryList!!.filter {
//                        it.id in addCategoryGroup.checkedButtonIds
//                    }.map {
//                        it.wordCount = 0
//                        it
//                    }.also { newList ->
//                        word.categoryList = newList
//                    }

                    //TODO FIX CATEGORY UPDATE COUNT INT
                    // UPDATE DIRECTLY WITH THE CATEGORY CHECKED LIST

//                    when {
//                        word.wordText.isBlank() -> showToast("Word cannot be empty")
//                        word.furiganaText.isBlank() -> showToast("Furigana cannot be empty")
//                        word.definitionText.isBlank() -> showToast("Definition cannot be empty")
//                        else -> {
//                            viewModel.updateWord(word, )
//                            showToast("You updated ${currentWord!!.wordText}")
//                            updateWordDialog.dismiss()
//                        }
//                    }
                }
            }

            updateWordDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

//    override fun onItemClickListener(word: Word) {
//        viewModel.updateIsForgottenWord(word, true)
//        viewModel.updateForgotCountWord(word)
//        updateWordDialog.dismiss()
//    }

    @Suppress("DEPRECATION")
    private fun getParcelable() {
        currentWord = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable(UPDATE_DIALOG_FRAGMENT_WORD_ARGS, Word::class.java)
        } else {
            arguments?.getParcelable(UPDATE_DIALOG_FRAGMENT_WORD_ARGS)
        }

        categoryList = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelableArrayList(
                UPDATE_DIALOG_FRAGMENT_CATEGORY_ARGS,
                Category::class.java
            )
        } else {
            arguments?.getParcelableArrayList(UPDATE_DIALOG_FRAGMENT_CATEGORY_ARGS)
        }
    }

    companion object {
        fun newInstance(word: Word, categoryList: List<Category>): UpdateWordDialogFragment {
            val dialogFragment = UpdateWordDialogFragment()
//            dialogFragment.arguments = Bundle().apply {
//                putParcelable(UPDATE_DIALOG_FRAGMENT_WORD_ARGS, word)
//                putParcelableArrayList(
//                    UPDATE_DIALOG_FRAGMENT_CATEGORY_ARGS,
//                    ArrayList(categoryList)
//                )
//            }
            return dialogFragment
        }

        private const val UPDATE_DIALOG_FRAGMENT_WORD_ARGS = "UPDATE_DIALOG_FRAGMENT_WORD_ARGS"
        private const val UPDATE_DIALOG_FRAGMENT_CATEGORY_ARGS =
            "UPDATE_DIALOG_FRAGMENT_CATEGORY_ARGS"
    }

}