package com.sawelo.wordmemorizer.window.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.databinding.WindowDrawWordFloatingBinding
import com.sawelo.wordmemorizer.util.StrokeManager
import com.sawelo.wordmemorizer.util.ViewUtils.addButtonInLayout
import com.sawelo.wordmemorizer.util.ViewUtils.showToast
import com.sawelo.wordmemorizer.util.callback.StrokeCallback
import com.sawelo.wordmemorizer.window.DialogWindow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FloatingDrawWordWindow(
    private val context: Context,
    private var initialText: String = "",
    private val parentWindow: DialogWindow,
    private val result: ((wordText: String) -> Unit)
) : DialogWindow(context), StrokeCallback {

    private var binding: WindowDrawWordFloatingBinding? = null
    private var strokeManager: StrokeManager? = null

    override fun setViews(layoutInflater: LayoutInflater): ViewGroup {
        binding = WindowDrawWordFloatingBinding.inflate(layoutInflater)
        return binding?.root ?: throw Exception("Binding cannot be null")
    }

    override fun beforeShowWindow() {
        // Instancing strokeManager
        strokeManager = StrokeManager(this@FloatingDrawWordWindow)
        strokeManager?.let {
            it.getDigitalInkRecognizer()
            binding?.windowDrawDrawingView?.setStrokeManager(it)
        }

        // Collect changes in wordCandidates flow
        windowCoroutineScope.launch {
            strokeManager?.wordCandidates?.collectLatest { candidates ->
                binding?.windowDrawRecommendationLayout?.removeAllViews()
                if (candidates.isNotEmpty()) {
                    binding?.windowDrawRecommendationLayout?.addButtonInLayout(
                        context, "Reset"
                    ) {
                        clearCanvas()
                    }
                    candidates.forEach {
                        binding?.windowDrawRecommendationLayout?.addButtonInLayout(
                            context, it.text
                        ) {
                            startTyping(it.text)
                        }
                    }
                }
            }
        }

        fadeVisibility()
        binding?.windowDrawFadeWindowBtn?.setOnClickListener {
            isDrawWindowFadeVisibility = !isDrawWindowFadeVisibility
            fadeVisibility()
        }
        binding?.windowDrawResetBtn?.setOnClickListener {
            backspaceTyping()
        }
        binding?.windowDrawCancelBtn?.setOnClickListener {
            closeWindow()
        }
        binding?.windowDrawOkBtn?.setOnClickListener {
            result.invoke(initialText)
            closeWindow()
        }

        updateDrawingWord()
    }

    override fun beforeCloseWindow() {
        parentWindow.revealWindow()
        strokeManager?.closeDigitalInkRecognizer()
        strokeManager = null
        binding = null
    }

    private fun startTyping(drawnText: String) {
        initialText += drawnText
        clearCanvas()
        updateDrawingWord()
    }

    private fun backspaceTyping() {
        if (initialText.isNotBlank()) {
            initialText = initialText.dropLast(1)
            clearCanvas()
            updateDrawingWord()
        }
    }

    private fun clearCanvas() {
        binding?.windowDrawDrawingView?.clear()
        binding?.windowDrawRecommendationLayout?.removeAllViews()
    }

    private fun updateDrawingWord() {
        binding?.windowDrawDrawingWordEt?.setText(initialText)
        binding?.windowDrawResetBtn?.isVisible = initialText.isNotBlank()
    }

    private fun fadeVisibility() {
        if (isDrawWindowFadeVisibility) {
            binding?.windowDrawFadeWindowBtn?.setImageResource(R.drawable.baseline_visibility_24)
            binding?.root?.alpha = .6F
        } else {
            binding?.windowDrawFadeWindowBtn?.setImageResource(R.drawable.baseline_visibility_off_24)
            binding?.root?.alpha = 1F
        }
    }

    override fun onFailure(message: String) {
        context.showToast("Character recognition failed: $message")
        closeWindow()
    }

    companion object {
        private var isDrawWindowFadeVisibility = false
    }
}