package com.greencopper.event.scheduleItem.ui.datepicker.dynamic

import android.content.Context
import android.util.AttributeSet
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.event.R
import com.greencopper.interfacekit.ui.HorizontalSpacingItemDecorator
import kotlin.math.abs
import kotlin.math.truncate

internal class ScrollableDatePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr) {

    private val linearLayoutManager: LinearLayoutManager = layoutManager as LinearLayoutManager
    private var decorator: ItemDecoration? = null

    init {
        if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
            linearLayoutManager.stackFromEnd = true
        }
        itemAnimator = null

        val margin = resources.getDimension(R.dimen.horizontal_margin).toInt()
        setPadding(margin, 0, margin, 0)
        clipToPadding = false
    }

    fun setDynamicDecorators() {
        val itemWidth = resources.getDimension(R.dimen.datepickerbutton_width)

        // show 66% of the next visible item
        val cellNextVisibleItemWidth = itemWidth * 0.67
        val widthToFitFullWidthItems = this.width - cellNextVisibleItemWidth - paddingStart

        var nbOfItemsThatFullyFitOnScreen = truncate(widthToFitFullWidthItems / itemWidth)

        var lessThanFourItemsFit = false
        if (nbOfItemsThatFullyFitOnScreen < 4) {
            nbOfItemsThatFullyFitOnScreen = 4.00
            lessThanFourItemsFit = true
        }

        val widthOfItemsThatFullyFitOnScreen = nbOfItemsThatFullyFitOnScreen * itemWidth
        val spacing =
            if (lessThanFourItemsFit)
                0
            else
                abs(widthToFitFullWidthItems - widthOfItemsThatFullyFitOnScreen) / nbOfItemsThatFullyFitOnScreen

        decorator?.let { removeItemDecoration(it) }
        decorator = HorizontalSpacingItemDecorator(
            spacing = spacing.toInt(),
        ).also { addItemDecoration(it) }
    }

    override fun scrollToPosition(position: Int) {
        val findFirstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
        val isBelowFirstVisibleItem = position <= findFirstVisibleItemPosition
        val findLastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition()
        val isAboveLastVisibleItem = findLastVisibleItemPosition <= position
        if (isBelowFirstVisibleItem || isAboveLastVisibleItem) {
            val scrollTo =
                if (isBelowFirstVisibleItem) MathUtils.clamp(position - 1, 0, position)
                else MathUtils.clamp(
                    position + 1,
                    0,
                    adapter?.itemCount?.let { it - 1 } ?: position)
            super.scrollToPosition(scrollTo)
        }
    }
}
