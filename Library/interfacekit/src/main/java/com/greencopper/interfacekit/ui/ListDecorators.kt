package com.greencopper.interfacekit.ui

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCell
import kotlin.math.roundToInt

public open class BottomDrawableItemDecorator(
    decoratorInfos: DecoratorInfos,
) : RecyclerView.ItemDecoration() {

    public open class DecoratorInfos(
        public val drawable: Drawable,
        public val showLast: Boolean,
        public val fullWidth: Boolean = true,
        public val drawableHorizontalPaddingDp: Int?,
    ) {
        public fun copy(): DecoratorInfos = DecoratorInfos(
            drawable.constantState?.newDrawable()?.mutate() ?: GradientDrawable(),
            showLast,
            fullWidth,
            drawableHorizontalPaddingDp
        )
    }

    //It seems like the ItemDecoration messes up the Drawable once it uses it.
    //Since others might use the Drawable (like WidgetCollectionCell), we create a copy just for the ItemDecoration
    private val _decoratorInfos: DecoratorInfos = decoratorInfos.copy()

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        var left: Int
        var right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right,
                parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }
        if (!_decoratorInfos.fullWidth) {
            val center = (right - left) / 2
            val halfDrawable = _decoratorInfos.drawable.intrinsicWidth / 2
            left = center - halfDrawable
            right = center + halfDrawable
        } else {
            _decoratorInfos.drawableHorizontalPaddingDp?.let {
                val padding = it.dpToPx()
                left += padding
                right -= padding
            }
        }
        val childCount = parent.childCount
        val lastIndex = if (_decoratorInfos.showLast) 1 else 2
        for (i in 0..childCount - lastIndex) {
            val child = parent.getChildAt(i)
            val shouldShowSeparator = shouldShowSeparator(parent, child)
            if (shouldShowSeparator) {
                val mBounds = Rect()
                parent.getDecoratedBoundsWithMargins(child, mBounds)
                val bottom: Int = mBounds.bottom + child.translationY.roundToInt()
                val top: Int = bottom - _decoratorInfos.drawable.intrinsicHeight
                _decoratorInfos.drawable.setBounds(
                    left,
                    top,
                    right,
                    bottom
                )
                _decoratorInfos.drawable.draw(canvas)
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val shouldShowSeparator = shouldShowSeparator(parent, view)
        if (shouldShowSeparator) {
            outRect.set(0, 0, 0, _decoratorInfos.drawable.intrinsicHeight)
        }
    }

    protected open fun shouldShowSeparator(recyclerView: RecyclerView, child: View): Boolean = true
}

public open class WidgetCollectionCellAwareItemDecorator(decoratorInfos: DecoratorInfos, private val excludeOuterMargins:Boolean) :
    BottomDrawableItemDecorator(decoratorInfos) {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        when (parent.findContainingViewHolder(view)) {
            is WidgetCollectionCell -> {
                val itemMargin = if(excludeOuterMargins) 0 else view.resources.getDimensionPixelSize(R.dimen.widget_min_margin)
                outRect.top = itemMargin
                outRect.bottom = itemMargin
            }
        }
    }

    override fun shouldShowSeparator(recyclerView: RecyclerView, child: View): Boolean {
        val currentViewHolder = recyclerView.findContainingViewHolder(child)
        val nextType = try {
            recyclerView.adapter?.getItemViewType(recyclerView.getChildAdapterPosition(child) + 1)
        } catch (e: IndexOutOfBoundsException) {
            null
        }

        return !(currentViewHolder is WidgetCollectionCell || nextType == WidgetCollectionCell.ADAPTER_TYPE)
    }
}

public class SimpleLineDecorator(
    tintColor: Int,
    showLast: Boolean,
    fullWidth: Boolean = true,
    drawableHorizontalPaddingDp: Int?,
) : BottomDrawableItemDecorator.DecoratorInfos(
    drawable = GradientDrawable().apply {
        setSize(1, 1.dpToPx())
        color = ColorStateList.valueOf(tintColor)
    },
    showLast = showLast,
    fullWidth = fullWidth,
    drawableHorizontalPaddingDp = drawableHorizontalPaddingDp,
)
