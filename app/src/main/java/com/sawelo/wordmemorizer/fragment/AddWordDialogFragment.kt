package com.sawelo.wordmemorizer.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atilika.kuromoji.jumandic.Token
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.activity.MainActivity
import com.sawelo.wordmemorizer.adapter.SimilarWordAdapter
import com.sawelo.wordmemorizer.data.Word
import com.sawelo.wordmemorizer.utils.ItemWordAdapterCallback
import com.sawelo.wordmemorizer.utils.MaterialToggleButton
import com.sawelo.wordmemorizer.utils.WordCommand
import com.sawelo.wordmemorizer.utils.WordUtils.isAll
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import dev.esnault.wanakana.core.Wanakana

class AddWordDialogFragment : DialogFragment(), ItemWordAdapterCallback {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var addWordDialog: AlertDialog
    private var categoryFragment: CategoryFragment? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val view = layoutInflater.inflate(R.layout.dialog_add_word, null)
            val builder = MaterialAlertDialogBuilder(activity).setView(view)
            addWordDialog = builder.create()

            val wordEt = view.findViewById<EditText>(R.id.dialog_addWord_et)
            val furiganaEt = view.findViewById<EditText>(R.id.dialog_addFurigana_et)
            val definitionEt = view.findViewById<EditText>(R.id.dialog_addDefinition_et)
            val similarWordRv = view.findViewById<RecyclerView>(R.id.dialog_similarWord_rv)
            val similarWordTv = view.findViewById<TextView>(R.id.dialog_similarWord_tv)
            val addCategoryGroup =
                view.findViewById<MaterialButtonToggleGroup>(R.id.dialog_addCategory_group)
            val addBtn: Button = view.findViewById(R.id.dialog_addWord_btn)

            val adapter = SimilarWordAdapter(this)
            similarWordRv.adapter = adapter
            similarWordRv.layoutManager = LinearLayoutManager(activity)

            val mainFragment = (activity as MainActivity)
                .supportFragmentManager.findFragmentByTag(MainFragment.MAIN_FRAGMENT_TAG)
            categoryFragment = mainFragment?.childFragmentManager
                ?.findFragmentByTag(viewModel.currentCategoryFragmentTag) as CategoryFragment?

            wordEt.doOnTextChanged { text, _, _, _ ->
                val wordString = text.toString()
                if (Wanakana.isJapanese(wordString)) {
                    val tokens: List<Token> = viewModel.tokenizer.tokenize(wordString)
                    val hiraganaTokens = tokens.map {
                        Wanakana.toHiragana(it.reading)
                    }
                    furiganaEt.setText(hiraganaTokens.joinToString())
                }
                viewModel.getAllWordsByWord(wordString).observe(this) {
                    similarWordTv.isVisible = it.isEmpty()
                    adapter.submitList(it)
                }
            }

            viewModel.getAllCategories().observe(activity) { categoryList ->
                for (category in categoryList) {
                    if (!category.isAll()) {
                        val button = MaterialToggleButton(
                            activity, null,
                            com.google.android.material.R.attr.materialButtonOutlinedStyle
                        ).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            text = category.categoryName
                            id = category.id
                        }
                        (addCategoryGroup as ViewGroup).addView(button)
                    }
                }
            }

            addBtn.setOnClickListener {
                val word = Word(
                    wordText = wordEt.text.toString(),
                    furiganaText = furiganaEt.text.toString(),
                    definitionText = definitionEt.text.toString(),
                    createdTimeMillis = System.currentTimeMillis(),
                )

                viewModel.getAllCategories().observe(activity) { categoryList ->
                    println("CATEGORY LIST $categoryList")
                    categoryList.filter {
                        it.id in addCategoryGroup.checkedButtonIds
                    }.also {
                        word.categoryList = it
                    }
                    println("CATEGORY IN WORD ${word.categoryList}")

                    when {
                        word.wordText.isBlank() -> showToast("Word cannot be empty")
                        word.furiganaText.isBlank() -> showToast("Furigana cannot be empty")
                        word.definitionText.isBlank() -> showToast("Definition cannot be empty")
                        else -> {
                            categoryFragment?.setWordCommand(WordCommand.INSERT_WORD)
                            viewModel.addWord(word) { wordId ->
                                addWordDialog.dismiss()

//                                categoryFragment?.scrollRecyclerView(wordId, WordCommand.INSERT_WORD)
                            }
                        }
                    }
                }
            }

            addWordDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onItemClickListener(word: Word) {
        categoryFragment?.setWordCommand(WordCommand.FORGOT_WORD)
        viewModel.updateIsForgottenWord(word, true) {
            addWordDialog.dismiss()

//            categoryFragment?.scrollRecyclerView(word.id, WordCommand.FORGOT_WORD)
        }
    }

    private fun showToast(text: String) {
        Toast
            .makeText(activity, text, Toast.LENGTH_SHORT)
            .show()
    }

}