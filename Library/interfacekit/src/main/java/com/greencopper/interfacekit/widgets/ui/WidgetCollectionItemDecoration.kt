package com.greencopper.interfacekit.widgets.ui

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.interfacekit.ui.DynamicVerticalSpacingItemDecorator
import com.greencopper.interfacekit.ui.dpToPx
import com.greencopper.interfacekit.widgets.ui.header.WidgetCollectionHeaderView
import com.greencopper.interfacekit.widgets.viewmodel.WidgetItemHolder

/**
 * Decorator used to pass a different spacing depending on the widget shown
 * If a bottom/top spacing is different between two consecutive widgets, the biggest one will be chosen
 * @param topMarginOverride If set, this value will be used for top margin instead of the widget's verticalMargin
 * @param bottomMarginOverride If set, this value will be used for bottom margin instead of the widget's verticalMargin
 * @see [DynamicVerticalSpacingItemDecorator]
 */
internal class WidgetCollectionItemDecoration(
    private val topMarginOverride: Int? = null,
    private val bottomMarginOverride: Int? = null,
) : DynamicVerticalSpacingItemDecorator(false) {
    override fun getCurrentSpacing(
        recycler: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        currentItemPosition: Int,
    ): Rect? {
        val currentItem = recycler.layoutManager?.findViewByPosition(currentItemPosition)
        return when (currentItem) {
            is WidgetCollectionHeaderView -> {
                val nextSpacingTop = getNextSpacing(
                    recycler,
                    adapter,
                    currentItemPosition + 1
                )?.bottom ?: 0

                val bottomSpacing = if (currentItem.hasCornerRadius) {
                    nextSpacingTop.takeIf { it > 0 } ?: 16.dpToPx().toInt()
                } else nextSpacingTop

                Rect(
                    0,
                    topMarginOverride ?: 0,
                    0,
                    bottomMarginOverride ?: bottomSpacing
                )
            }
            is WidgetLayout<*> ->
                Rect(0, topMarginOverride ?: currentItem.verticalMargin, 0, bottomMarginOverride ?: currentItem.verticalMargin)

            else -> null
        }
    }

    override fun getNextSpacing(
        recycler: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        nextItemPosition: Int,
    ): Rect? {
        val widgetLayout =
            adapter.onCreateViewHolder(recycler, adapter.getItemViewType(nextItemPosition)).itemView as? WidgetLayout<*>
        return widgetLayout?.let {
            Rect(0, it.verticalMargin, 0, it.verticalMargin)
        }
    }
}

/**
 * Returns what vertical padding should be applied to an item depending on its place in a list of items and widgets.
 * Doesn't support different item spacing at the moment
 * @param listItems A list of items which contains items of any type and [WidgetItemHolder]s
 * @param index The index of the current item
 * @param containerTopPadding The absolute top padding applied if the first item is not a widget
 * @param containerBottomPadding The absolute bottom padding applied if the last item is not a widget
 * @param itemToItemPadding The space between regular items
 * @param itemToWidgetPadding The space between a regular item and a widget
 * @return A top/bottom [Pair]
 */
public fun getTopBottomPaddingWidgetAware(
    listItems: List<Any>,
    index: Int,
    columnFactor: Int,
    containerTopPadding: Int,
    containerBottomPadding: Int,
    itemToItemPadding: Int,
    itemToWidgetPadding: Int,
): Pair<Int, Int> {
    val item = listItems.getOrNull(index) ?: return Pair(0, 0)
    val top = when (item) {
        is WidgetItemHolder -> if (index == 0) item.topPadding else 0
        else -> {
            if (index < columnFactor && listItems[0] !is WidgetItemHolder)
                containerTopPadding
            else 0
        }
    }
    val bottom = when (item) {
        is WidgetItemHolder -> if (index == listItems.lastIndex) item.bottomPadding else {
            listItems[index + 1].let {
                when (it) {
                    is WidgetItemHolder -> maxOf(it.topPadding, item.bottomPadding)
                    else -> itemToWidgetPadding
                }
            }
        }

        else -> if (index == listItems.lastIndex) containerBottomPadding else {
            when (listItems[index + 1]) {
                is WidgetItemHolder -> itemToWidgetPadding
                else -> itemToItemPadding
            }
        }
    }
    return Pair(top, bottom)
}
