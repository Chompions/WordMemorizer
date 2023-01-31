package com.sawelo.wordmemorizer.window

import android.content.Context
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.util.Constants.isDrawWindowFadeVisibility
import com.sawelo.wordmemorizer.util.StrokeManager
import com.sawelo.wordmemorizer.util.ViewUtils.addButtonInLayout
import com.sawelo.wordmemorizer.util.callback.StrokeCallback
import com.sawelo.wordmemorizer.view.DialogWindowScrollView
import com.sawelo.wordmemorizer.view.DrawingView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FloatingDrawWordWindow(
    private val context: Context,
    private var initialText: String = "",
    private val parentWindow: DialogWindow,
    private val result: ((wordText: String) -> Unit)
): DialogWindow(context, R.layout.window_draw_word_floating), StrokeCallback {
    private lateinit var strokeManager: StrokeManager

    private var scrollView: DialogWindowScrollView? = null
    private var drawingView: DrawingView? = null
    private var drawingWordEt: EditText? = null
    private var drawnTextResetBtn: ImageButton? = null
    private var fadeVisibilityBtn: ImageButton? = null
    private var recommendationLayout: LinearLayout? = null
    private var cancelBtn: Button? = null
    private var okBtn: Button? = null

    override fun setViews(parent: ViewGroup) {
        scrollView = parent as DialogWindowScrollView
        drawingView = parent.findViewById(R.id.windowDraw_drawingView)
        drawingWordEt = parent.findViewById(R.id.windowDraw_drawingWordEt)
        drawnTextResetBtn = parent.findViewById(R.id.windowDraw_reset_btn)
        fadeVisibilityBtn = parent.findViewById(R.id.windowDraw_fadeWindow_btn)
        recommendationLayout = parent.findViewById(R.id.windowDraw_recommendationLayout)
        cancelBtn = parent.findViewById(R.id.windowDraw_cancel_btn)
        okBtn = parent.findViewById(R.id.windowDraw_ok_btn)
    }

    override fun clearViews() {
        scrollView = null
        drawingView = null
        drawingWordEt = null
        drawnTextResetBtn = null
        fadeVisibilityBtn = null
        recommendationLayout = null
        cancelBtn = null
        okBtn = null
    }

    override fun beforeShowWindow(coroutineScope: CoroutineScope) {
        // Instancing strokeManager
        strokeManager = StrokeManager(this@FloatingDrawWordWindow)
        strokeManager.getDigitalInkRecognizer()
        drawingView?.setStrokeManager(strokeManager)

        // Collect changes in wordCandidates flow
        coroutineScope.launch {
            strokeManager.wordCandidates.collectLatest { candidates ->
                recommendationLayout?.removeAllViews()
                if (candidates.isNotEmpty()) {
                    recommendationLayout?.addButtonInLayout(context, "Reset") {
                        clearCanvas()
                    }
                    candidates.forEach {
                        recommendationLayout?.addButtonInLayout(context, it.text) {
                            startTyping(it.text)
                        }
                    }
                }
            }
        }

        fadeVisibility()
        fadeVisibilityBtn?.setOnClickListener {
            isDrawWindowFadeVisibility = !isDrawWindowFadeVisibility
            fadeVisibility()
        }
        drawnTextResetBtn?.setOnClickListener {
            backspaceTyping()
        }
        cancelBtn?.setOnClickListener {
            closeWindow()
        }
        okBtn?.setOnClickListener {
            result.invoke(initialText)
            closeWindow()
        }

        updateDrawingWord()
    }

    override fun beforeCloseWindow(coroutineScope: CoroutineScope) {
        parentWindow.revealWindow()
        strokeManager.closeDigitalInkRecognizer()
    }

    override fun setAdditionalParams(params: WindowManager.LayoutParams?) {}

    private fun fadeVisibility() {
        if (isDrawWindowFadeVisibility) {
            fadeVisibilityBtn?.setImageResource(R.drawable.baseline_visibility_24)
            scrollView?.alpha = .6F
        } else {
            fadeVisibilityBtn?.setImageResource(R.drawable.baseline_visibility_off_24)
            scrollView?.alpha = 1F
        }
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
        drawingView?.clear()
        recommendationLayout?.removeAllViews()
    }

    private fun updateDrawingWord() {
        drawingWordEt?.setText(initialText)
        drawnTextResetBtn?.isVisible = initialText.isNotBlank()
    }

    override fun onFailure(message: String) {
        showToast("Character recognition failed: $message")
        closeWindow()
    }
}