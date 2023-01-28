package com.sawelo.wordmemorizer.window

import android.content.Context
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
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
    private val initialText: String?,
    private val parentWindow: DialogWindow,
    private val result: ((wordText: String) -> Unit)
): DialogWindow(context, R.layout.window_draw_word_floating), StrokeCallback {
    private lateinit var strokeManager: StrokeManager

    private var scrollView: DialogWindowScrollView? = null
    private var drawingView: DrawingView? = null
    private var drawingWordTv: TextView? = null
    private var drawnTextResetBtn: ImageButton? = null
    private var fadeVisibilityBtn: ImageButton? = null
    private var recommendationLayout: LinearLayout? = null
    private var cancelBtn: Button? = null
    private var okBtn: Button? = null

    private val drawnCharacterList: MutableList<Char> = mutableListOf()

    override fun setViews(parent: ViewGroup) {
        scrollView = parent as DialogWindowScrollView
        drawingView = parent.findViewById(R.id.windowDraw_drawingView)
        drawingWordTv = parent.findViewById(R.id.windowDraw_drawingWord)
        drawnTextResetBtn = parent.findViewById(R.id.windowDraw_reset_btn)
        fadeVisibilityBtn = parent.findViewById(R.id.windowDraw_fadeWindow_btn)
        recommendationLayout = parent.findViewById(R.id.windowDraw_recommendationLayout)
        cancelBtn = parent.findViewById(R.id.windowDraw_cancel_btn)
        okBtn = parent.findViewById(R.id.windowDraw_ok_btn)
    }

    override fun clearViews() {
        scrollView = null
        drawingView = null
        drawingWordTv = null
        drawnTextResetBtn = null
        fadeVisibilityBtn = null
        recommendationLayout = null
        cancelBtn = null
        okBtn = null
    }

    override fun beforeShowWindow(coroutineScope: CoroutineScope) {
        // Instancing strokeManager
        strokeManager = StrokeManager(this@FloatingDrawWordWindow)
        strokeManager.getDigitalInkRecognizer() { finishedDownloading ->
            if (!finishedDownloading) {
                showToast("Downloading character recognizer, please wait")
                closeWindow()
            } else {
                showToast("Download complete")
            }
        }
        drawingView?.setStrokeManager(strokeManager)

        // Collect changes in wordCandidates flow
        coroutineScope.launch {
            strokeManager.wordCandidates.collectLatest { candidates ->
                // Remove all button before adding new ones
                recommendationLayout?.removeAllViews()

                if (candidates.isNotEmpty()) {
                    // Add clear button in recommendation list
                    recommendationLayout?.addButtonInLayout(context, "Reset") {
                        clearCanvas()
                    }
                    // Add character candidates button in recommendation list
                    candidates.forEach {
                        recommendationLayout?.addButtonInLayout(context, it.text) {
                            startTyping(it.text.toList())
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
            result.invoke(drawnCharacterList.joinToString(""))
            closeWindow()
        }

        /**
         * If initialText is provided and not null, then add text to drawnCharacterList and
         * display it without initializing clearCanvas(). This is important because canvasBitmap
         * has not been initialized at this point and will causes fatal exception
         */
        if (initialText != null) {
            drawnCharacterList.addAll(initialText.toList())
            drawingWordTv?.text = drawnCharacterList.joinToString("")
            drawnTextResetBtn?.isVisible = true
        }
    }

    override fun beforeCloseWindow(coroutineScope: CoroutineScope) {
        parentWindow.revealWindow()
    }

    private fun fadeVisibility() {
        if (isDrawWindowFadeVisibility) {
            fadeVisibilityBtn?.setImageResource(R.drawable.baseline_visibility_24)
            scrollView?.alpha = .6F
        } else {
            fadeVisibilityBtn?.setImageResource(R.drawable.baseline_visibility_off_24)
            scrollView?.alpha = 1F
        }
    }

    private fun startTyping(drawnText: List<Char>) {
        drawnCharacterList.addAll(drawnText)
        updateDrawingWord()
    }

    private fun backspaceTyping() {
        drawnCharacterList.removeLast()
        updateDrawingWord()
    }

    private fun clearCanvas() {
        drawingView?.clear()
        recommendationLayout?.removeAllViews()
    }

    private fun updateDrawingWord() {
        clearCanvas()
        if (drawnCharacterList.isNotEmpty()) {
            drawingWordTv?.text = drawnCharacterList.joinToString("")
            drawnTextResetBtn?.isVisible = true
        } else {
            drawingWordTv?.text = context.getText(R.string.your_character_here)
            drawnTextResetBtn?.isVisible = false
        }
    }

    override fun onFailure(message: String) {
        showToast("Character recognition failed: $message")
        closeWindow()
    }
}