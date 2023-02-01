package com.sawelo.wordmemorizer.window.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import com.sawelo.wordmemorizer.activity.EditWordActivity
import com.sawelo.wordmemorizer.data.WordRepository
import com.sawelo.wordmemorizer.data.data_class.Word
import com.sawelo.wordmemorizer.databinding.WindowAddWordInfoFloatingBinding
import com.sawelo.wordmemorizer.util.FloatingDialogUtils
import com.sawelo.wordmemorizer.window.DialogWindow
import kotlinx.coroutines.launch

class FloatingInfoWordWindow(
    private val context: Context,
    private val wordRepository: WordRepository,
    private val itemWord: Word,
): DialogWindow(context) {

    private var binding: WindowAddWordInfoFloatingBinding? = null
    private var floatingDialogUtils: FloatingDialogUtils? = null

    override fun setViews(layoutInflater: LayoutInflater): ViewGroup {
        binding = WindowAddWordInfoFloatingBinding.inflate(layoutInflater)
        return binding?.root ?: throw Exception("Binding cannot be null")
    }

    override fun setParams(params: WindowManager.LayoutParams): WindowManager.LayoutParams {
        return super.setParams(params).apply {
            width = context.resources.displayMetrics.widthPixels - 200
        }
    }

    override fun beforeShowWindow() {
        floatingDialogUtils = FloatingDialogUtils(wordRepository)

        binding?.infoWordText?.text = itemWord.wordText
        binding?.infoFuriganaText?.text = itemWord.furiganaText
        binding?.infoDefinitionText?.text = itemWord.definitionText

        binding?.infoWordText?.setOnLongClickListener {
            windowCoroutineScope.launch {
                val categoryList = floatingDialogUtils?.getAllCategories()
                if (categoryList != null) {
                    EditWordActivity.startActivity(
                        context, itemWord.wordId, categoryList
                    )
                }
                closeWindow()
            }
            true
        }
        binding?.infoOk?.setOnClickListener {
            closeWindow()
        }
    }

    override fun beforeCloseWindow() {
        binding = null
    }
}