package com.greencopper.interfacekit.filtering.filteringbar.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.SelectableColor
import com.greencopper.interfacekit.databinding.FilteringBarViewBinding
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarData
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarState
import kotlinx.coroutines.CoroutineScope

public class FilteringBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    private val binding = FilteringBarViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var filteringBarCellBackgroundColors: SelectableColor
    private lateinit var filteringBarCellBorderColors: SelectableColor
    private lateinit var filteringBarCellTitleColors: SelectableColor
    private lateinit var filteringBarCellFonts: FilteringBarTextStyle

    private lateinit var viewLifecycleScope: CoroutineScope

    init {
        orientation = VERTICAL
    }

    public var isSetup: Boolean = false
        private set

    private var hasButtons: Boolean = false

    public fun setup(
        filteringBarColors: FilteringBarColor,
        filteringBarTextStyle: FilteringBarTextStyle,
        transparentBackground: Boolean = false,
        lifecycleScope: CoroutineScope,
    ) {
        this.filteringBarCellBackgroundColors = filteringBarColors.button.background
        this.filteringBarCellBorderColors = filteringBarColors.button.border
        this.filteringBarCellTitleColors = filteringBarColors.button.title

        if (!transparentBackground) {
            binding.filteringBarLayout.setBackgroundColor(filteringBarColors.background)
        }
        binding.filteringBarSeparator.setBackgroundColor(filteringBarColors.border)

        filteringBarCellFonts = filteringBarTextStyle

        viewLifecycleScope = lifecycleScope

        isSetup = true
    }

    public fun insertButton(
        buttonState: FilteringBarState.ButtonState,
    ) {
        insertButton(
            isCheckedAtSetup = buttonState.isCheckedAtSetup,
            buttonState.default,
            buttonState.selected,
            onButtonToggled = buttonState.onButtonToggled,
        )
    }

    public fun insertButton(
        isCheckedAtSetup: Boolean,
        defaultState: FilteringBarCell.ButtonState.State,
        selectedState: FilteringBarCell.ButtonState.State?,
        onButtonToggled: (Boolean) -> Unit,
    ) {

        if (defaultState.title.isNullOrEmpty() && (selectedState == null || selectedState.title.isNullOrEmpty())) {
            binding.iconsLayout.addView(
            FilteringBarCell.Icon(context).apply {
                setup(
                    filteringBarCellBackgroundColors,
                    filteringBarCellBorderColors,
                    filteringBarCellTitleColors,
                    FilteringBarCell.ButtonState(defaultState, selectedState),
                    viewLifecycleScope,
                ) {
                    toggle()
                    onButtonToggled(isChecked)
                }
                isChecked = isCheckedAtSetup
            }, 0
            )
        } else {
            binding.iconsLayout.addView(
                FilteringBarCell.Label(context).apply {
                    setup(
                        filteringBarCellFonts.button,
                        filteringBarCellBackgroundColors,
                        filteringBarCellBorderColors,
                        filteringBarCellTitleColors,
                        R.dimen.card_corner_radius,
                        FilteringBarCell.ButtonState(defaultState, selectedState),
                        viewLifecycleScope,
                    ) {
                        toggle()
                        onButtonToggled(isChecked)
                    }

                }.apply { isChecked = isCheckedAtSetup }, 0)
        }

        hasButtons = true
        updateViewVisibility()
    }


    public fun update(data: FilteringBarData?) {
        removeFilterTagViews()

        data?.filters?.forEach { filter ->
            val filterButton = FilteringBarCell.Label(context)
            filterButton.tag = TAG_FILTER_BAR
            filterButton.setup(
                filteringBarCellFonts.button,
                filteringBarCellBackgroundColors,
                filteringBarCellBorderColors,
                filteringBarCellTitleColors,
                R.dimen.filtering_bar_cell_tag_radius,
                FilteringBarCell.ButtonState(FilteringBarCell.ButtonState.State(filter.name, null, filter.name)),
                viewLifecycleScope,
            ) {
                filter.onTap()
            }

            filterButton.isChecked = filter.isSelected
            filterButton.setArrowVisibility(filter.showArrow)
            binding.labelsLayout.addView(filterButton)
        }

        updateViewVisibility()
    }

    private fun removeFilterTagViews() {
        binding.labelsLayout.children.toList()
            .filter { it.tag == TAG_FILTER_BAR }
            .forEach(binding.labelsLayout::removeView)
    }

    private fun updateViewVisibility() {
        val hasFilters = binding.labelsLayout.children.any { it.tag == TAG_FILTER_BAR }

        binding.filteringBarSeparator.isVisible = hasFilters && hasButtons
        binding.filteringBarLayout.isVisible = hasFilters || hasButtons
    }

    private companion object {
        const val TAG_FILTER_BAR = "tag.filterBar"
    }
}
