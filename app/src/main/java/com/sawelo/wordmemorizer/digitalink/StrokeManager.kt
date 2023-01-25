package com.sawelo.wordmemorizer.digitalink

import android.view.MotionEvent
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.*
import com.sawelo.wordmemorizer.util.StrokeCallback
import kotlinx.coroutines.flow.MutableStateFlow

class StrokeManager(private val callback: StrokeCallback) {
    private var strokeBuilder = Ink.Stroke.builder()
    private var inkBuilder = Ink.builder()

    private var recognizer: DigitalInkRecognizer? = null
    private var modelIdentifier:  DigitalInkRecognitionModelIdentifier? = null
    private var model: DigitalInkRecognitionModel? = null

    private val remoteModelManager = RemoteModelManager.getInstance()

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

    fun getDigitalInkRecognizer(isModelDownloading: (finished: Boolean) -> Unit) {
        modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("ja-JP")
        model = DigitalInkRecognitionModel.builder(modelIdentifier!!).build()
        remoteModelManager.isModelDownloaded(model!!).addOnSuccessListener {isDownloaded ->
            if (!isDownloaded) {
                isModelDownloading.invoke(false)
                remoteModelManager.download(model!!, DownloadConditions.Builder().build())
                    .addOnSuccessListener {
                        isModelDownloading.invoke(true)
                        recognizer = DigitalInkRecognition.getClient(
                            DigitalInkRecognizerOptions.builder(model!!).build()
                        )
                    }
            } else {
                recognizer = DigitalInkRecognition.getClient(
                    DigitalInkRecognizerOptions.builder(model!!).build()
                )
            }
        }
    }

}