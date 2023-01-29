package com.sawelo.wordmemorizer.util

import android.view.MotionEvent
import com.google.mlkit.vision.digitalink.*
import com.sawelo.wordmemorizer.util.callback.StrokeCallback
import kotlinx.coroutines.flow.MutableStateFlow

class StrokeManager(private val callback: StrokeCallback) {
    private var strokeBuilder = Ink.Stroke.builder()
    private var inkBuilder = Ink.builder()

    private var recognizer: DigitalInkRecognizer? = null

    val wordCandidates = MutableStateFlow(emptyList<RecognitionCandidate>())

    fun addNewTouchEvent(event: MotionEvent) {
        val action = event.actionMasked
        val x = event.x
        val y = event.y
        val t = System.currentTimeMillis()

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                strokeBuilder = Ink.Stroke.builder()
                strokeBuilder.addPoint(Ink.Point.create(x, y, t))
            }
            MotionEvent.ACTION_MOVE -> strokeBuilder.addPoint(Ink.Point.create(x, y, t))
            MotionEvent.ACTION_UP -> {
                strokeBuilder.addPoint(Ink.Point.create(x, y, t))
                inkBuilder.addStroke(strokeBuilder.build())

                val ink = inkBuilder.build()
                recognizer?.recognize(ink)?.addOnSuccessListener {
                    wordCandidates.value = it.candidates
                }?.addOnFailureListener {
                    callback.onFailure(it.message.toString())
                }
            }
        }
    }

    fun resetCurrentInk() {
        inkBuilder = Ink.builder()
        wordCandidates.value = emptyList()
    }

    fun getDigitalInkRecognizer() {
        val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("ja-JP")
        val model = DigitalInkRecognitionModel.builder(modelIdentifier!!).build()
        recognizer = DigitalInkRecognition.getClient(
            DigitalInkRecognizerOptions.builder(model).build()
        )
    }

    fun closeDigitalInkRecognizer() {
        recognizer?.close()
        recognizer = null
    }

}