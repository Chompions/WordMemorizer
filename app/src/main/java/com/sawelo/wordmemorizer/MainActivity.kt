package com.sawelo.wordmemorizer

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sawelo.wordmemorizer.data.Word
import com.sawelo.wordmemorizer.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ItemWordAdapterCallback {
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private val viewModel: MainViewModel by viewModels()
    private var adapter: WordAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setRecyclerView()
        binding.activityMainFab.setOnClickListener {
            AddDialogFragment().show(supportFragmentManager, null)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuOptions_reload) {
            viewModel.resetCount() {
                showToast("Word count reset")
            }
            return true
        }
        return false
    }

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
        println("You're pressing ${word.kanjiText}")
    }

    private fun setRecyclerView() {
        adapter = WordAdapter(this)

        recyclerView = binding.activityMainRv
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.allWordsLiveData.observe(this) {
            adapter?.submitList(it)
        }
    }

    private fun showToast(text: String) {
        Toast
            .makeText(this, text, Toast.LENGTH_SHORT)
            .show()
    }

}