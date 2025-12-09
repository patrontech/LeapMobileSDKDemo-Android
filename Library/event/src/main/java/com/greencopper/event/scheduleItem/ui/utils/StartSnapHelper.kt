package com.greencopper.event.scheduleItem.ui.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.max

internal class StartSnapHelper : LinearSnapHelper() {

    companion object {
        private const val MILLISECONDS_PER_INCH = 100f
        private const val MAX_SCROLL_ON_FLING_DURATION_MS = 1000
    }

    private var helper: OrientationHelper? = null
    private var context: Context? = null
    private var scroller: Scroller? = null

    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        if (recyclerView != null) {
            context = recyclerView.context
            scroller = Scroller(context, DecelerateInterpolator())
        } else {
            scroller = null
            context = null
        }
        super.attachToRecyclerView(recyclerView)
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? =
        findFirstView(layoutManager, orientationHelper(layoutManager))

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        val out = IntArray(2)
        out[0] = distanceToStart(targetView, orientationHelper(layoutManager))
        return out
    }

    private fun findFirstView(
        layoutManager: RecyclerView.LayoutManager?,
        helper: OrientationHelper
    ): View? {
        if (layoutManager == null || layoutManager.childCount == 0) return null

        var absClosest = Integer.MAX_VALUE
        var closestView: View? = null
        val start = helper.startAfterPadding

        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i)
            val childStart = helper.getDecoratedStart(child)
            val absDistanceToStart = abs(childStart - start)
            if (absDistanceToStart < absClosest) {
                absClosest = absDistanceToStart
                closestView = child
            }
        }
        return closestView
    }

    override fun createScroller(layoutManager: RecyclerView.LayoutManager): RecyclerView.SmoothScroller? {
        if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            return super.createScroller(layoutManager)
        }
        val context = context ?: return null
        return object : LinearSmoothScroller(context) {
            override fun onTargetFound(
                targetView: View,
                state: RecyclerView.State,
                action: Action
            ) {
                val snapDistance = calculateDistanceToFinalSnap(layoutManager, targetView)
                val distanceX = snapDistance[0]
                val distanceY = snapDistance[1]
                val distanceTime = calculateTimeForDeceleration(abs(distanceX))
                val time = max(1, kotlin.math.min(MAX_SCROLL_ON_FLING_DURATION_MS, distanceTime))
                action.update(distanceX, distanceY, time, DecelerateInterpolator())
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float =
                MILLISECONDS_PER_INCH / displayMetrics.densityDpi
        }
    }

    private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
        // We don't actually want it at the start of the view, we want it at the end of the previous
        // view; because spacing is the same for each button, we scroll to the end position of the previous view by finding
        // the decoration size so we can subtract the original snap position by the decoration size again

        var start = 0
        var end = 0

        when(ViewCompat.getLayoutDirection(targetView)) {
            ViewCompat.LAYOUT_DIRECTION_LTR -> {
                start = targetView.left
                end = targetView.right
            }
            ViewCompat.LAYOUT_DIRECTION_RTL -> {
                start = targetView.right
                end = targetView.left
            }
        }

        val childStart = helper.getDecoratedStart(targetView)
        val extraOffset = childStart - start
        // if the spacing/decoration is zero, the snap wont align itself properly because every item is touching each other.
        // this will occur on smaller screens where being able to see 5 items minimum is more important than having them spaced.
        // to account for this: since each item is hardcoded to 60dp width, instead of subtracting the decorator, we subtract a fourth of the distance of the target view.
        var fourth = 0
        // -1 because the decorator always seems to take at least 1 pixel, so it is always -1 instead of zero.
        if (extraOffset >= -1) {
            val length = end - start
            fourth = abs(length * 0.25f).toInt()
        }

        return childStart - fourth
    }

    private fun orientationHelper(layoutManager: RecyclerView.LayoutManager?): OrientationHelper {
        if (helper == null) {
            helper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return helper!!
    }
}