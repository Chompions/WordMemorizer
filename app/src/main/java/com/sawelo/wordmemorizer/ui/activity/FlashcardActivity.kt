package com.sawelo.wordmemorizer.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithInfo
import com.sawelo.wordmemorizer.databinding.ActivityFlashcardBinding
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FlashcardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFlashcardBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var flashcardList: MutableList<WordWithInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.activityFlashcardToolbar.setNavigationOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            binding.activityFlashcardProgressBar.isVisible = true
            flashcardList = viewModel.getWordWithInfoForFlashcards() as MutableList<WordWithInfo>
            binding.activityFlashcardProgressBar.isVisible = false

            showQuestion()
        }

        binding.activityFlashcardShowAnswerBtn.setOnClickListener {
            showAnswer()
        }

        binding.activityFlashcardRememberBtn.setOnClickListener {
            viewModel.updateRemember(flashcardList.first().word)
            flashcardList.remove(flashcardList.first())
            showQuestion()
        }

        binding.activityFlashcardForgotBtn.setOnClickListener {
            viewModel.updateForgot(flashcardList.first().word)
            flashcardList.first().wordInfo.rememberCount -= 1
            flashcardList.shuffle()
            showQuestion()
        }
    }

    private fun showQuestion() {
        if (flashcardList.isNotEmpty()) {
            binding.activityFlashcardQuestionTv.text = flashcardList.first().word.wordText

            binding.activityFlashcardQuestionLayout.isVisible = true
            binding.activityFlashcardAnswerLayout.isVisible = false
        } else {
            finish()
        }
    }

    private fun showAnswer() {
        flashcardList.first().also {
            binding.activityFlashcardAnswerWordTv.text = it.word.wordText
            binding.activityFlashcardAnswerFuriganaTv.text = it.word.furiganaText
            binding.activityFlashcardAnswerDefinitionTv.text = it.word.definitionText
            binding.activityFlashcardRememberCountTv.text = it.wordInfo.rememberCount.toString()
        }

        binding.activityFlashcardQuestionLayout.isVisible = false
        binding.activityFlashcardAnswerLayout.isVisible = true
    }
}