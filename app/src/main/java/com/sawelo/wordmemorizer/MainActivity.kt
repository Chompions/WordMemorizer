package com.sawelo.wordmemorizer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sawelo.wordmemorizer.data.Word
import com.sawelo.wordmemorizer.data.WordDao
import com.sawelo.wordmemorizer.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), WordAdapterCallback {
    @Inject
    lateinit var wordDao: WordDao
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WordAdapter
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setRecyclerView()
        binding.activityMainFab.setOnClickListener {
            setDialog()
        }
    }

    override fun onItemHideBtnClickListener(word: Word) {
        lifecycleScope.launch {
            viewModel.forgotWord(word, adapter, recyclerView)
            showSnackBar("Word forgotten: ${word.kanjiText}")
        }
    }

    override fun onItemLongClickListener(word: Word) {
        lifecycleScope.launch {
            viewModel.deleteWord(word, adapter)
            showSnackBar("Word deleted: ${word.kanjiText}")
        }
    }

    private fun setRecyclerView() {
        adapter = WordAdapter(this)

        recyclerView = binding.activityMainRv
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.insertAllWords(adapter)
        }
    }

    private fun setDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add, binding.root, false)
        val builder = AlertDialog.Builder(this).setView(view)
        val dialog = builder.create()

        val addBtn: Button = view.findViewById(R.id.dialog_add_btn)

        addBtn.setOnClickListener {
            val kanjiEt = view.findViewById<EditText>(R.id.dialog_kanji_et).text.toString()
            val hiraganaEt = view.findViewById<EditText>(R.id.dialog_hiragana_et).text.toString()
            val definitionEt =
                view.findViewById<EditText>(R.id.dialog_definition_et).text.toString()

            lifecycleScope.launch {
                val word = Word(kanjiEt, hiraganaEt, definitionEt)
                val rowId = wordDao.insertWord(word)
                if (rowId == -1L) {
                    viewModel.forgotWord(word, adapter, recyclerView)
                    showSnackBar("Word already exist: ${word.kanjiText}")
                } else {
                    viewModel.insertNewWord(word, adapter, recyclerView)
                    showSnackBar("Word inserted: ${word.kanjiText}")
                }
                dialog.cancel()
            }
        }
        dialog.show()
    }

    private fun showSnackBar(text: String) {
        Snackbar
            .make(binding.root, text, Toast.LENGTH_SHORT)
            .show()
    }

}