package com.sawelo.wordmemorizer.fragment.dialog

import android.app.Dialog
import android.os.Build
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atilika.kuromoji.jumandic.Token
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.activity.MainActivity
import com.sawelo.wordmemorizer.adapter.AddWordAdapter
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.fragment.CategoryFragment
import com.sawelo.wordmemorizer.fragment.MainFragment
import com.sawelo.wordmemorizer.util.WordUtils.isAll
import com.sawelo.wordmemorizer.util.callback.ItemWordAdapterCallback
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import dev.esnault.wanakana.core.Wanakana
import kotlinx.coroutines.launch

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

            val adapter = AddWordAdapter(this)
            similarWordRv.adapter = adapter
            similarWordRv.layoutManager = LinearLayoutManager(activity)

            val mainFragment = (activity as MainActivity)
                .supportFragmentManager.findFragmentByTag(MainFragment.MAIN_FRAGMENT_TAG)
            categoryFragment = mainFragment?.childFragmentManager
                ?.findFragmentByTag(viewModel.currentCategoryFragmentTag) as CategoryFragment?

            wordEt.doOnTextChanged { text, _, _, _ ->
                lifecycleScope.launch {
                    val wordString = text.toString()
                    if (Wanakana.isJapanese(wordString)) {
                        val tokens: List<Token> = viewModel.tokenizer.tokenize(wordString)
                        val hiraganaTokens = tokens.map {
                            Wanakana.toHiragana(it.reading)
                        }
                        furiganaEt.setText(hiraganaTokens.joinToString())
                    }
                    viewModel.getAllWordsByWord(wordString) {
                        if (wordString.isNotBlank() && it.isNotEmpty()) {
                            similarWordTv.isVisible = false
                            adapter.submitList(it)
                        } else {
                            similarWordTv.isVisible = true
                            adapter.submitList(emptyList())
                        }
                    }
                }
            }

            @Suppress("DEPRECATION") val categoryList = if (Build.VERSION.SDK_INT >= 33) {
                arguments?.getParcelableArrayList(ADD_DIALOG_FRAGMENT_ARGS, Category::class.java)
            } else {
                arguments?.getParcelableArrayList(ADD_DIALOG_FRAGMENT_ARGS)
            }

            if (categoryList != null) {
                for (category in categoryList) {
                    if (!category.isAll()) {
                        val button = MaterialButton(
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

                addBtn.setOnClickListener {
                    val word = Word(
                        wordText = wordEt.text.toString(),
                        furiganaText = furiganaEt.text.toString(),
                        definitionText = definitionEt.text.toString(),
                        createdTimeMillis = System.currentTimeMillis(),
                    )

                    categoryList.filter {
                        it.id in addCategoryGroup.checkedButtonIds
                    }.map {
                        it.wordCount = 0
                        it
                    }.also { newList ->
                        word.categoryList = newList
                    }

                    when {
                        word.wordText.isBlank() -> showToast("Word cannot be empty")
                        word.furiganaText.isBlank() -> showToast("Furigana cannot be empty")
                        word.definitionText.isBlank() -> showToast("Definition cannot be empty")
                        else -> {
                            viewModel.addWord(word)
                            addWordDialog.dismiss()
                        }
                    }
                }
            }

            addWordDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onItemClickListener(word: Word) {
        viewModel.updateIsForgottenWord(word, true)
        viewModel.updateForgotCountWord(word)
        addWordDialog.dismiss()
    }

    private fun showToast(text: String) {
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

        const val ADD_DIALOG_FRAGMENT_ARGS = "ADD_DIALOG_FRAGMENT_ARGS"
    }

}