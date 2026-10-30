package com.protonmail.jobforandroid.weather.fragments

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/** Allows the inner forecast pager to scroll until its edge, then gives the gesture to the screen pager. */
class NestedScrollableHost @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var initialX = 0f
    private var initialY = 0f
    private var gestureDirection: Int = 0

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val childPager = findViewById<ViewPager2>(com.protonmail.jobforandroid.weather.R.id.forecastPager)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.x
                initialY = event.y
                gestureDirection = 0
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - initialX
                val dy = event.y - initialY
                if (gestureDirection == 0 && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                    gestureDirection = if (abs(dx) > abs(dy)) 1 else 2
                }
                if (gestureDirection == 1) {
                    val canInnerScroll = childPager?.canScrollHorizontally(if (dx > 0) -1 else 1) == true
                    parent?.requestDisallowInterceptTouchEvent(canInnerScroll)
                } else if (gestureDirection == 2) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                parent?.requestDisallowInterceptTouchEvent(false)
        }
        return super.dispatchTouchEvent(event)
    }

}
