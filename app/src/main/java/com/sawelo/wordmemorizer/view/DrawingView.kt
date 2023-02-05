package com.sawelo.wordmemorizer.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.sawelo.wordmemorizer.util.StrokeManager

class DrawingView @JvmOverloads constructor(
    context: Context?,
    attributeSet: AttributeSet? = null
) :
    View(context, attributeSet) {

    private val currentStrokePaint: Paint = Paint().apply {
        color = -0xff01 // pink.
        isAntiAlias = true
        // Set stroke width based on display density.
        strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3.toFloat(),
            resources.displayMetrics
        )
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint: Paint = Paint().apply {
        val modeFlags = context?.resources?.configuration
            ?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        when (modeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> color = Color.WHITE
            Configuration.UI_MODE_NIGHT_NO -> color = Color.GRAY
        }
        alpha = 100
        textAlign = Paint.Align.CENTER
        textSize = 60F
    }

    private val canvasPaint: Paint = Paint(Paint.DITHER_FLAG)
    private val currentStroke: Path = Path()
    private lateinit var drawCanvas: Canvas
    private lateinit var canvasBitmap: Bitmap
    private lateinit var strokeManager: StrokeManager

    private var isCanvasEmpty = true

    fun setStrokeManager(strokeManager: StrokeManager) {
        this.strokeManager = strokeManager
    }

    override fun onSizeChanged(
        width: Int,
        height: Int,
        oldWidth: Int,
        oldHeight: Int
    ) {
        canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (isCanvasEmpty) {
            canvas.drawText("Draw your character", width / 2F, height / 2F, textPaint)
        }

        canvas.drawBitmap(canvasBitmap, 0f, 0f, canvasPaint)
        canvas.drawPath(currentStroke, currentStrokePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val x = event.x
        val y = event.y
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                isCanvasEmpty = false
                currentStroke.moveTo(x, y)
            }
            MotionEvent.ACTION_MOVE -> currentStroke.lineTo(x, y)
            MotionEvent.ACTION_UP -> {
                currentStroke.lineTo(x, y)
                drawCanvas.drawPath(currentStroke, currentStrokePaint)
                currentStroke.reset()
            }
        }
        strokeManager.addNewTouchEvent(event)
        invalidate()
        return true
    }

    fun clear() {
        currentStroke.reset()
        onSizeChanged(
            canvasBitmap.width,
            canvasBitmap.height,
            canvasBitmap.width,
            canvasBitmap.height
        )
        strokeManager.resetCurrentInk()
        isCanvasEmpty = true
    }

}