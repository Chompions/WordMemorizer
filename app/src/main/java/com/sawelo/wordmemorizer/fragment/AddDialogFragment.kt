package com.sawelo.wordmemorizer.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
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
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.utils.WordCommand
import com.sawelo.wordmemorizer.activity.MainActivity
import com.sawelo.wordmemorizer.adapter.SimilarWordAdapter
import com.sawelo.wordmemorizer.data.Word
import com.sawelo.wordmemorizer.utils.ItemWordAdapterCallback
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import dev.esnault.wanakana.core.Wanakana
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AddDialogFragment : DialogFragment(), ItemWordAdapterCallback {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var addWordDialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val view = layoutInflater.inflate(R.layout.dialog_add_word, null)
            val builder = AlertDialog.Builder(activity).setView(view)
            addWordDialog = builder.create()

            val wordEt = view.findViewById<EditText>(R.id.dialog_addWord_et)
            val furiganaEt = view.findViewById<EditText>(R.id.dialog_addFurigana_et)
            val definitionEt = view.findViewById<EditText>(R.id.dialog_addDefinition_et)
            val similarWordRv = view.findViewById<RecyclerView>(R.id.dialog_similarWord_rv)
            val similarWordTv = view.findViewById<TextView>(R.id.dialog_similarWord_tv)
            val addBtn: Button = view.findViewById(R.id.dialog_addWord_btn)

            val adapter = SimilarWordAdapter(this)
            similarWordRv.adapter = adapter
            similarWordRv.layoutManager = LinearLayoutManager(activity)
            viewModel.similarWordListLiveData.observe(this) {
                similarWordTv.isVisible = it.isEmpty()
                adapter.submitList(it)
            }

            wordEt.doOnTextChanged { text, _, _, _ ->
                val wordString = text.toString()
                if (Wanakana.isJapanese(wordString)) {
                    val tokens: List<Token> = viewModel.tokenizer.tokenize(wordString)
                    val hiraganaTokens = tokens.map {
                        Wanakana.toHiragana(it.reading)
                    }
                    furiganaEt.setText(hiraganaTokens.joinToString())
                }
                if (wordString.isNotBlank()) {
                    runBlocking {
                        viewModel.searchSimilarWords(wordString)
                    }
                } else viewModel.clearSimilarWordList()
            }

            addBtn.setOnClickListener {
                val word = Word(
                    wordText = wordEt.text.toString(),
                    furiganaText = furiganaEt.text.toString(),
                    definitionText = definitionEt.text.toString(),
                    createdTimeMillis = System.currentTimeMillis()
                )

                lifecycleScope.launch {
                    viewModel.addWord(word) {
                        addWordDialog.dismiss()
                        viewModel.clearSimilarWordList()
                        (activity as MainActivity).scrollRecyclerView(it, WordCommand.INSERT_WORD)

                        Toast.makeText(
                            activity,
                            "Word inserted: ${word.wordText}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            addWordDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onItemClickListener(word: Word) {
        addWordDialog.dismiss()
        viewModel.clearSimilarWordList()
        (activity as MainActivity).scrollRecyclerView(word.id, WordCommand.FORGOT_WORD)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.clearSimilarWordList()
    }
}