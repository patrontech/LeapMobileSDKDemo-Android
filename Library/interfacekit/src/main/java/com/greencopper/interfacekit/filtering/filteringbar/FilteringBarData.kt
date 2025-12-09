package com.greencopper.interfacekit.filtering.filteringbar

import com.greencopper.interfacekit.filtering.FilterId

public data class FilteringBarData(
    val filters: List<Filter>
) {
    public data class Filter(
        val id: FilterId,
        val index: Int,
        val name: String,
        val showArrow: Boolean,
        val isSelected: Boolean,
        val onTap: () -> Unit
    )
}
