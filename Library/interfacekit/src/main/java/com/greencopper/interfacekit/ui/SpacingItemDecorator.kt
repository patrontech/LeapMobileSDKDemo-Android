package com.greencopper.interfacekit.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

public class HorizontalSpacingItemDecorator(
    private val spacing: Int = 0,
    private val verticalPadding: Int = 0,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val isRtl = view.layoutDirection == View.LAYOUT_DIRECTION_RTL

        outRect.top = verticalPadding
        outRect.bottom = verticalPadding
        if (parent.getChildAdapterPosition(view) != 0) {
            if (isRtl) {
                outRect.right = spacing
            } else {
                outRect.left = spacing
            }
        }
    }
}

public class VerticalSpacingItemDecorator(
    private val spacing: Int = 0,
    private val horizontalPadding: Int = 0,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        outRect.right = horizontalPadding
        outRect.left = horizontalPadding
        if (parent.getChildAdapterPosition(view) != 0) {
            outRect.top = spacing
        }
    }
}

/**
 * Decorator used to pass a different spacing depending on the view type
 * If a bottom/top spacing is different between two consecutive views, the biggest one will be chosen
 * @param excludeOuterMargins If true, won't show first/last top/bottom spacing.
 */
public abstract class DynamicVerticalSpacingItemDecorator(
    private val excludeOuterMargins: Boolean,
) : RecyclerView.ItemDecoration() {

    /**
     * Returns the expected spacing in pixels of the current view being decorated
     */
    protected abstract fun getCurrentSpacing(
        recycler: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        currentItemPosition: Int,
    ): Rect?

    /**
     * Returns the expected spacing in pixels of the view next of the one being decorated
     */
    protected abstract fun getNextSpacing(
        recycler: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        nextItemPosition: Int,
    ): Rect?

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val isRtl = view.layoutDirection == View.LAYOUT_DIRECTION_RTL
        val adapter = parent.adapter ?: return
        val currentItemPosition = parent.getChildAdapterPosition(view)
        val currentSpacing = getCurrentSpacing(parent, adapter, currentItemPosition) ?: return

        outRect.right = if (isRtl) currentSpacing.left else currentSpacing.right
        outRect.left = if (isRtl) currentSpacing.right else currentSpacing.left

        if (currentItemPosition == 0 && !excludeOuterMargins) {
            outRect.top = currentSpacing.top
        }

        val currentBottomSpacing = currentSpacing.bottom
        if (currentItemPosition == adapter.itemCount - 1 && !excludeOuterMargins) {
            outRect.bottom = currentBottomSpacing
        } else if (currentItemPosition < adapter.itemCount - 1) {
            val nextBottomSpacing = getNextSpacing(parent, adapter, currentItemPosition + 1)?.top ?: 0
            outRect.bottom = maxOf(nextBottomSpacing, currentBottomSpacing)
        }
    }
}

/**
 * Decorator used to pass a different spacing depending on the view type
 * If a bottom/top spacing is different between two consecutive views, the biggest one will be chosen
 * @param decoratorMap Map associating a view type ID to its spacing (in PX) in four directions
 * @param excludeOuterMargins If true, won't show first/last top/bottom spacing.
 * @see [DynamicVerticalSpacingItemDecorator]
 */
public class MappedDynamicVerticalSpacingItemDecorator(
    private val decoratorMap: Map<Int, Rect>,
    excludeOuterMargins: Boolean,
) : DynamicVerticalSpacingItemDecorator(excludeOuterMargins) {

    private fun getSpacingOfPosition(adapter: RecyclerView.Adapter<*>, itemPosition: Int): Rect? {
        val currentType = adapter.getItemViewType(itemPosition)
        return decoratorMap[currentType]
    }

    override fun getCurrentSpacing(
        recycler: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        currentItemPosition: Int,
    ): Rect? = getSpacingOfPosition(adapter, currentItemPosition)

    override fun getNextSpacing(
        recycler: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        nextItemPosition: Int,
    ): Rect? = getSpacingOfPosition(adapter, nextItemPosition)
}
