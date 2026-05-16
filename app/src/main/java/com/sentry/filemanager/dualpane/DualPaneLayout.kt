/*
 * Copyright (c) 2026 eZee + Claude
 * SentryOS Project
 */

package com.sentry.filemanager.dualpane

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import kotlin.math.max
import kotlin.math.min

/**
 * A horizontal LinearLayout with a draggable divider between two panes.
 * Children at index 0 and 2 are the panes; index 1 is the divider handle.
 */
class DualPaneLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    // Split ratio — 0.5 = equal halves
    var splitRatio: Float = 0.5f
        set(value) {
            field = value.coerceIn(MIN_RATIO, MAX_RATIO)
            requestLayout()
        }

    private var dragging = false
    private var lastTouchX = 0f

    companion object {
        private const val MIN_RATIO = 0.25f
        private const val MAX_RATIO = 0.75f
    }

    init {
        orientation = HORIZONTAL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val totalWidth = MeasureSpec.getSize(widthMeasureSpec)
        if (childCount < 3) return

        val divider = getChildAt(1)
        val dividerWidth = divider.measuredWidth

        val available = totalWidth - dividerWidth
        val pane1Width = (available * splitRatio).toInt()
        val pane2Width = available - pane1Width

        val heightSpec = MeasureSpec.makeMeasureSpec(
            MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY
        )

        getChildAt(0).measure(
            MeasureSpec.makeMeasureSpec(pane1Width, MeasureSpec.EXACTLY), heightSpec
        )
        getChildAt(2).measure(
            MeasureSpec.makeMeasureSpec(pane2Width, MeasureSpec.EXACTLY), heightSpec
        )
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (childCount < 3) return false
        val divider = getChildAt(1)
        return when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                val dividerLeft = getChildAt(0).width
                val dividerRight = dividerLeft + divider.width
                val hit = ev.x >= dividerLeft - 16 && ev.x <= dividerRight + 16
                if (hit) { lastTouchX = ev.x; dragging = true }
                hit
            }
            else -> dragging
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!dragging) return false
        when (ev.action) {
            MotionEvent.ACTION_MOVE -> {
                val totalWidth = width.toFloat()
                val dividerWidth = getChildAt(1).width
                val available = totalWidth - dividerWidth
                splitRatio = ((ev.x - dividerWidth / 2f) / available)
                    .coerceIn(MIN_RATIO, MAX_RATIO)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> dragging = false
        }
        return true
    }
}
