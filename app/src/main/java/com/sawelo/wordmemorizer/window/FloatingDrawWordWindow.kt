package com.sawelo.wordmemorizer.window

import android.content.Context
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.util.StrokeManager
import com.sawelo.wordmemorizer.util.callback.StrokeCallback
import com.sawelo.wordmemorizer.util.ViewUtils.addButtonInLayout
import com.sawelo.wordmemorizer.view.DrawingView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FloatingDrawWordWindow(
    private val context: Context,
    private val result: ((wordText: String) -> Unit)
): DialogWindow(context, R.layout.window_draw_word_floating), StrokeCallback {
    private lateinit var strokeManager: StrokeManager

    private var drawingView: DrawingView? = null
    private var drawingWordTv: TextView? = null
    private var drawnTextResetBtn: ImageButton? = null
    private var recommendationLayout: LinearLayout? = null
    private var cancelBtn: Button? = null
    private var okBtn: Button? = null

    private val drawnCharacterList: MutableList<String> = mutableListOf()

    override fun setViews(parent: ViewGroup) {
        drawingView = parent.findViewById(R.id.windowDraw_drawingView)
        drawingWordTv = parent.findViewById(R.id.windowDraw_drawingWord)
        drawnTextResetBtn = parent.findViewById(R.id.windowDraw_reset_btn)
        recommendationLayout = parent.findViewById(R.id.windowDraw_recommendationLayout)
        cancelBtn = parent.findViewById(R.id.windowDraw_cancel_btn)
        okBtn = parent.findViewById(R.id.windowDraw_ok_btn)
    }

    override fun clearViews() {
        drawingView = null
        drawingWordTv = null
        drawnTextResetBtn = null
        recommendationLayout = null
        cancelBtn = null
        okBtn = null
    }

    override fun beforeShowWindow(coroutineScope: CoroutineScope) {
        // Instancing strokeManager
        strokeManager = StrokeManager(this@FloatingDrawWordWindow)
        strokeManager.getDigitalInkRecognizer() { finishedDownloading ->
            if (!finishedDownloading) {
                Toast.makeText(
                    context, "Downloading character recognizer, please wait", Toast.LENGTH_SHORT
                ).show()
                closeWindow()
            } else {
                Toast.makeText(
                    context, "Download complete", Toast.LENGTH_SHORT
                ).show()
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
                            startTyping(it.text)
                        }
                    }
                }
            }
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
    }

    override fun beforeCloseWindow(coroutineScope: CoroutineScope) {}


    private fun startTyping(drawnText: String) {
        drawnCharacterList.add(drawnText)
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
        Toast
            .makeText(context, "Character recognition failed: $message", Toast.LENGTH_SHORT)
            .show()
        closeWindow()
    }
}