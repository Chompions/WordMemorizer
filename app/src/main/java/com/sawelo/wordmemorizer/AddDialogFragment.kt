package com.sawelo.wordmemorizer

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.sawelo.wordmemorizer.data.Word

class AddDialogFragment : DialogFragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val view = layoutInflater.inflate(R.layout.dialog_add_word, null)
            val builder = AlertDialog.Builder(it).setView(view)
            val addWordDialog = builder.create()

            val addBtn: Button = view.findViewById(R.id.dialog_add_btn)
            addBtn.setOnClickListener {
                val kanjiEt = view.findViewById<EditText>(R.id.dialog_kanji_et).text.toString()
                val hiraganaEt =
                    view.findViewById<EditText>(R.id.dialog_hiragana_et).text.toString()
                val definitionEt =
                    view.findViewById<EditText>(R.id.dialog_definition_et).text.toString()
                val word = Word(
                    kanjiText = kanjiEt,
                    hiraganaText = hiraganaEt,
                    definitionText = definitionEt
                )

                viewModel.searchSimilarWords(word) { isEmpty ->
                    if (isEmpty) {
                        viewModel.stopCollectingSimilarWords()
                        viewModel.addWord(word) {
                            addWordDialog.dismiss()
                            Toast.makeText(
                                requireContext(),
                                "Word inserted: ${word.kanjiText}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        SimilarWordDialogFragment(word).show(
                            parentFragmentManager,
                            null
                        )
                    }
                }
            }
            addWordDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}