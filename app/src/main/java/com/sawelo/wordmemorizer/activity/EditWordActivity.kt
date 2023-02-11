package com.sawelo.wordmemorizer.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.sawelo.wordmemorizer.data.data_class.relation_ref.WordWithCategories
import com.sawelo.wordmemorizer.databinding.ActivityEditWordBinding
import com.sawelo.wordmemorizer.util.ViewUtils.showToast
import com.sawelo.wordmemorizer.viewmodel.UpdateWordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditWordActivity : AppCompatActivity() {
    private val viewModel: UpdateWordViewModel by viewModels()

    private lateinit var binding: ActivityEditWordBinding
    private lateinit var wordWithCategories: WordWithCategories

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditWordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.activityEditWordToolbar.setNavigationOnClickListener {
            finish()
        }

        setWordAndCategory()
        setUpdateButton()
        setDeleteButton()
    }

    private fun setWordAndCategory() {
        val wordIdExtra = intent.getIntExtra(EDIT_WORD_ACTIVITY_ID_EXTRA, 0)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                binding.activityEditWordProgressIndicator.isVisible = true

                try {
                    wordWithCategories = viewModel.getWordWithCategoriesById(wordIdExtra)
                    val word = wordWithCategories.wordWithInfo.word
                    binding.activityEditWordAddWordEt.setText(word.wordText)
                    binding.activityEditWordAddFuriganaEt.setText(word.furiganaText)
                    binding.activityEditWordAddDefinitionEt.setText(word.definitionText)

                    getCategoryList()

                } catch (e: Exception) {
                    this@EditWordActivity.showToast(e.message)
                } finally {
                    binding.activityEditWordProgressIndicator.isVisible = false
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun getCategoryList() {
        viewModel.getAllCategories().first().let { categoryWithInfoList ->
            for (categoryWithInfo in categoryWithInfoList) {
                val button = MaterialButton(
                    this, null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle
                ).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    text = categoryWithInfo.category.categoryName
                    id = categoryWithInfo.category.categoryId
                }
                (binding.activityEditWordGroup as ViewGroup).addView(button)
            }
            for (checkedCategory in wordWithCategories.categories) {
                binding.activityEditWordGroup.check(checkedCategory.categoryId)
            }
        }
    }

    private fun setUpdateButton() {
        binding.activityEditWordUpdateWordBtn.setOnClickListener {
            lifecycleScope.launch {
                val updatedWord = wordWithCategories.wordWithInfo.word.copy(
                    wordText = binding.activityEditWordAddWordEt.text.toString(),
                    furiganaText = binding.activityEditWordAddFuriganaEt.text.toString(),
                    definitionText = binding.activityEditWordAddDefinitionEt.text.toString()
                )
                val updatedWordWithInfo = wordWithCategories.wordWithInfo.copy(
                    word = updatedWord
                )

                val allCategories = viewModel.getAllCategories().first().map { it.category }
                val updatedWordWithCategories = WordWithCategories(
                    updatedWordWithInfo,
                    allCategories.filter {
                        it.categoryId in (binding.activityEditWordGroup.checkedButtonIds)
                    }
                )

                when {
                    updatedWord.wordText.isBlank() -> showToast("Word cannot be empty")
                    updatedWord.furiganaText.isBlank() -> showToast("Furigana cannot be empty")
                    updatedWord.definitionText.isBlank() -> showToast("Definition cannot be empty")
                    updatedWordWithCategories.categories.isEmpty() -> showToast("Category cannot be empty")

                    else -> {
                        binding.activityEditWordProgressIndicator.isVisible = true
                        try {
                            viewModel.updateWord(wordWithCategories, updatedWordWithCategories)
                            finish()
                        } catch (e: Exception) {
                            this@EditWordActivity.showToast(e.message)
                        } finally {
                            binding.activityEditWordProgressIndicator.isVisible = false
                        }
                    }
                }
            }

        }
    }

    private fun setDeleteButton() {
        binding.activityEditWordDeleteWordBtn.setOnClickListener {
            lifecycleScope.launch {
                binding.activityEditWordProgressIndicator.isVisible = true
                try {
                    viewModel.deleteWord(wordWithCategories.wordWithInfo.word)
                    finish()
                } catch (e: Exception) {
                    this@EditWordActivity.showToast(e.message)
                } finally {
                    binding.activityEditWordProgressIndicator.isVisible = false
                }
            }
        }
    }

    companion object {
        fun startActivity(context: Context?, wordId: Int) {
            val intent = Intent(context, EditWordActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(EDIT_WORD_ACTIVITY_ID_EXTRA, wordId)
            context?.startActivity(intent)
        }

        private const val EDIT_WORD_ACTIVITY_ID_EXTRA = "EDIT_WORD_ACTIVITY_ID_EXTRA"
    }
}