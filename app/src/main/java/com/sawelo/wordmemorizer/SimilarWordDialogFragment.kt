package com.sawelo.wordmemorizer

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sawelo.wordmemorizer.data.Word

class SimilarWordDialogFragment(private val word: Word) : DialogFragment(),
    ItemWordAdapterCallback {

    private val viewModel: MainViewModel by activityViewModels()
    private var forgotSimilarWordDialog: Dialog? = null

    override fun onItemHideBtnClickListener(word: Word) {
        viewModel.forgotWord(word) {
            showToast("Word forgotten: ${word.kanjiText}")
        }
    }

    override fun onItemLongClickListener(word: Word) {
        viewModel.deleteWord(word) {
            showToast("Word deleted: ${word.kanjiText}")
        }
    }

    override fun onItemClickListener(word: Word) {
        viewModel.forgotWord(word) {
            viewModel.stopCollectingSimilarWords()
            dismissAllDialog()
            showToast("Word forgotten: ${word.kanjiText}")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val view = layoutInflater.inflate(R.layout.dialog_forgot_word, null)
            val builder = AlertDialog.Builder(it).setView(view)
            forgotSimilarWordDialog = builder.create()

            val dialogAdapter = WordAdapter(this)
            viewModel.similarWordsLiveData.observe(this) { words ->
                if (words.isNotEmpty()) {
                    dialogAdapter.submitList(words)
                }
            }

            val dialogRecyclerView: RecyclerView = view.findViewById(R.id.dialog_rv)
            dialogRecyclerView.layoutManager = LinearLayoutManager(it)
            dialogRecyclerView.adapter = dialogAdapter

            val differentBtn: Button = view.findViewById(R.id.dialog_different_btn)
            differentBtn.setOnClickListener {
                viewModel.addWord(word) {
                    viewModel.stopCollectingSimilarWords()
                    dismissAllDialog()
                    showToast("Word inserted: ${word.kanjiText}")
                }
            }

            forgotSimilarWordDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDismiss(dialog: DialogInterface) {
        viewModel.stopCollectingSimilarWords()
        super.onDismiss(dialog)
    }

    private fun dismissAllDialog() {
        val fragments = parentFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is DialogFragment) fragment.dismiss()
        }
    }

    private fun showToast(text: String) {
        Toast
            .makeText(requireContext(), text, Toast.LENGTH_SHORT)
            .show()
    }
}