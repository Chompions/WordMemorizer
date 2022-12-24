package com.sawelo.wordmemorizer.activity

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.adapter.MainWordAdapter
import com.sawelo.wordmemorizer.data.Word
import com.sawelo.wordmemorizer.databinding.ActivityMainBinding
import com.sawelo.wordmemorizer.fragment.AddDialogFragment
import com.sawelo.wordmemorizer.utils.EndOffsetItemDecoration
import com.sawelo.wordmemorizer.utils.ItemWordAdapterCallback
import com.sawelo.wordmemorizer.utils.RecyclerViewUtils.scrollToWordId
import com.sawelo.wordmemorizer.utils.WordCommand
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ItemWordAdapterCallback {
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MainWordAdapter
    private lateinit var animation: Animation
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animation = AnimationUtils.loadAnimation(this, R.anim.blink_animation)
        setRecyclerView()
        binding.activityMainToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuOptions_reload -> {
                    runBlocking {
                        viewModel.resetCount() {
                            showToast("Word count reset")
                        }
                    }
                    true
                }
                else -> false
            }
        }
        binding.activityMainFab.setOnClickListener {
            AddDialogFragment().show(supportFragmentManager, null)
        }
    }

    override fun onItemForgotBtnClickListener(word: Word) {
        runBlocking {
            viewModel.setIsForgottenWord(word, true)
        }
    }

    override fun onItemHideBtnClickListener(word: Word) {
        runBlocking {
            viewModel.setIsForgottenWord(word, false)
            viewModel.setForgotCountIncreaseWord(word) {
                showToast("Word forgotten: ${word.wordText}")
            }
        }
    }

    override fun onItemLongClickListener(word: Word) {
        runBlocking {
            viewModel.deleteWord(word) {
                showToast("Word deleted: ${word.wordText}")
            }
        }
    }

    override fun onItemClickListener(word: Word) {
        println("You're pressing ${word.wordText}")
    }

    private fun setRecyclerView() {
        adapter = MainWordAdapter(this)

        recyclerView = binding.activityMainRv
        recyclerView.layoutManager = LinearLayoutManager(this)

        val offset = binding.activityMainFab.layoutParams.height + 100
        recyclerView.addItemDecoration(EndOffsetItemDecoration(offset))
        recyclerView.adapter = adapter

        viewModel.allWordListLiveData.observe(this) {
            adapter.submitList(it)
        }
    }

    private fun showToast(text: String) {
        Toast
            .makeText(this, text, Toast.LENGTH_SHORT)
            .show()
    }

    fun scrollRecyclerView(wordId: Int, wordCommand: WordCommand) {
        when (wordCommand) {
            WordCommand.FORGOT_WORD -> {
                scrollToWordId(wordId, adapter, recyclerView) { viewHolder ->
                    (viewHolder as MainWordAdapter.WordViewHolder).forgotBtn.performClick()
                }
            }
            WordCommand.INSERT_WORD -> {
                scrollToWordId(wordId, adapter, recyclerView) { viewHolder ->
                    viewHolder.itemView.startAnimation(animation)
                }
            }
        }
    }
}