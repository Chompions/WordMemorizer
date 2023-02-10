package com.sawelo.wordmemorizer.window.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import com.sawelo.wordmemorizer.activity.EditWordActivity
import com.sawelo.wordmemorizer.data.data_class.entity.Word
import com.sawelo.wordmemorizer.databinding.WindowAddWordInfoFloatingBinding
import com.sawelo.wordmemorizer.window.DialogWindow
import kotlinx.coroutines.launch

class FloatingInfoWordWindow(
    private val context: Context,
    private val itemWord: Word,
): DialogWindow(context) {

    private var binding: WindowAddWordInfoFloatingBinding? = null

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
        binding?.infoWordText?.text = itemWord.wordText
        binding?.infoFuriganaText?.text = itemWord.furiganaText
        binding?.infoDefinitionText?.text = itemWord.definitionText

        binding?.infoWordText?.setOnLongClickListener {
            windowCoroutineScope.launch {
                EditWordActivity.startActivity(
                    context, itemWord.wordId
                )
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