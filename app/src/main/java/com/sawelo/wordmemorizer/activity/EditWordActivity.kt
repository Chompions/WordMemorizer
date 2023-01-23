package com.sawelo.wordmemorizer.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.data.data_class.WordWithCategories
import com.sawelo.wordmemorizer.databinding.ActivityEditWordBinding
import com.sawelo.wordmemorizer.util.WordUtils.isAll
import com.sawelo.wordmemorizer.util.WordUtils.showToast
import com.sawelo.wordmemorizer.viewmodel.UpdateWordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditWordActivity : AppCompatActivity() {
    private val viewModel: UpdateWordViewModel by viewModels()
    private lateinit var binding: ActivityEditWordBinding
    private lateinit var wordWithCategories: WordWithCategories
    private var categoryList: List<Category>? = null

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
                    binding.activityEditWordAddWordEt.setText(wordWithCategories.word.wordText)
                    binding.activityEditWordAddFuriganaEt.setText(wordWithCategories.word.furiganaText)
                    binding.activityEditWordAddDefinitionEt.setText(wordWithCategories.word.definitionText)

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
    private fun getCategoryList() {
        categoryList = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableArrayListExtra(EDIT_WORD_ACTIVITY_CATEGORY_LIST_EXTRA, Category::class.java)
        } else {
            intent.getParcelableArrayListExtra(EDIT_WORD_ACTIVITY_CATEGORY_LIST_EXTRA)
        }

        if (categoryList != null) {
            for (category in categoryList!!) {
                if (!category.isAll()) {
                    val button = MaterialButton(
                        this, null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle
                    ).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        text = category.categoryName
                        id = category.categoryId
                    }
                    (binding.activityEditWordGroup as ViewGroup).addView(button)
                }
            }
            for (checkedCategory in wordWithCategories.categories) {
                binding.activityEditWordGroup.check(checkedCategory.categoryId)
            }
        }
    }

    private fun setUpdateButton() {
        binding.activityEditWordUpdateWordBtn.setOnClickListener {
            val updatedWord = wordWithCategories.word.copy(
                wordText = binding.activityEditWordAddWordEt.text.toString(),
                furiganaText = binding.activityEditWordAddFuriganaEt.text.toString(),
                definitionText = binding.activityEditWordAddDefinitionEt.text.toString()
            )

            val updatedWordWithCategories = WordWithCategories(
                updatedWord,
                categoryList!!.filter {
                    it.categoryId in (binding.activityEditWordGroup.checkedButtonIds)
                }
            )

            when {
                wordWithCategories.word.wordText.isBlank() -> showToast("Word cannot be empty")
                wordWithCategories.word.furiganaText.isBlank() -> showToast("Furigana cannot be empty")
                wordWithCategories.word.definitionText.isBlank() -> showToast("Definition cannot be empty")
                else -> {
                    lifecycleScope.launch {
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
                    viewModel.deleteWord(wordWithCategories.word)
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
        fun startActivity(activity: Activity?, wordId: Int, categoryList: List<Category>) {
            val intent = Intent(activity, EditWordActivity::class.java)
            intent.putExtra(EDIT_WORD_ACTIVITY_ID_EXTRA, wordId)
            intent.putExtra(EDIT_WORD_ACTIVITY_CATEGORY_LIST_EXTRA, ArrayList(categoryList))
            activity?.startActivity(intent)
        }

        private const val EDIT_WORD_ACTIVITY_ID_EXTRA = "EDIT_WORD_ACTIVITY_ID_EXTRA"
        private const val EDIT_WORD_ACTIVITY_CATEGORY_LIST_EXTRA = "EDIT_WORD_ACTIVITY_CATEGORY_LIST_EXTRA"
    }
}