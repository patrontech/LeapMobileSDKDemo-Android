package com.greencopper.interfacekit.filtering.filteringbar

import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarCell

public data class FilteringBarState(
    val buttons: List<ButtonState>,
    val filteringBarData: FilteringBarData?,
) {
    public data class ButtonState(
        val default: FilteringBarCell.ButtonState.State,
        val selected: FilteringBarCell.ButtonState.State?,
        val isCheckedAtSetup: Boolean,
        val onButtonToggled: (Boolean) -> Unit,
    )
}
