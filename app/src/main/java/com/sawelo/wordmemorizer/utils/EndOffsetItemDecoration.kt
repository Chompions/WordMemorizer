package com.sawelo.wordmemorizer.utils

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


class EndOffsetItemDecoration(
    private var mOffsetPx: Int = 0,
    private var mOffsetDrawable: Drawable? = null
    ) : ItemDecoration() {

    private var mOrientation = 0

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val itemCount = state.itemCount
        if (parent.getChildAdapterPosition(view) != itemCount - 1) {
            return
        }
        mOrientation = (parent.layoutManager as LinearLayoutManager?)!!.orientation
        if (mOrientation == LinearLayoutManager.HORIZONTAL) {
            if (mOffsetPx > 0) {
                outRect.right = mOffsetPx
            } else if (mOffsetDrawable != null) {
                outRect.right = mOffsetDrawable!!.intrinsicWidth
            }
        } else if (mOrientation == LinearLayoutManager.VERTICAL) {
            if (mOffsetPx > 0) {
                outRect.bottom = mOffsetPx
            } else if (mOffsetDrawable != null) {
                outRect.bottom = mOffsetDrawable!!.intrinsicHeight
            }
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (mOffsetDrawable == null) {
            return
        }
        if (mOrientation == LinearLayoutManager.HORIZONTAL) {
            drawOffsetHorizontal(c, parent)
        } else if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawOffsetVertical(c, parent)
        }
    }

    private fun drawOffsetHorizontal(canvas: Canvas, parent: RecyclerView) {
        val parentTop = parent.paddingTop
        val parentBottom = parent.height - parent.paddingBottom
        val lastChild: View = parent.getChildAt(parent.childCount - 1)
        val lastChildLayoutParams = lastChild.layoutParams as RecyclerView.LayoutParams
        val offsetDrawableLeft: Int = lastChild.right + lastChildLayoutParams.rightMargin
        val offsetDrawableRight = offsetDrawableLeft + mOffsetDrawable!!.intrinsicWidth
        mOffsetDrawable!!.setBounds(
            offsetDrawableLeft,
            parentTop,
            offsetDrawableRight,
            parentBottom
        )
        mOffsetDrawable!!.draw(canvas)
    }

    private fun drawOffsetVertical(canvas: Canvas, parent: RecyclerView) {
        val parentLeft = parent.paddingLeft
        val parentRight = parent.width - parent.paddingRight
        val lastChild: View = parent.getChildAt(parent.childCount - 1)
        val lastChildLayoutParams = lastChild.layoutParams as RecyclerView.LayoutParams
        val offsetDrawableTop: Int = lastChild.bottom + lastChildLayoutParams.bottomMargin
        val offsetDrawableBottom = offsetDrawableTop + mOffsetDrawable!!.intrinsicHeight
        mOffsetDrawable!!.setBounds(
            parentLeft,
            offsetDrawableTop,
            parentRight,
            offsetDrawableBottom
        )
        mOffsetDrawable!!.draw(canvas)
    }
}