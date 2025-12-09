package com.greencopper.interfacekit.filtering.filterselector

import com.greencopper.interfacekit.filtering.FilterId

public data class FilterSelectorData(
    val filters: Map<FilterId, List<Cell>>,
    val onClearTap: (List<FilterId>) -> Unit
) {

    public sealed class Cell {
        public data class Title(val title: String): Cell()
        public data class Option(
            val filterId: FilterId,
            val label: String,
            val isActive: Boolean,
            val onTap: () -> Unit
        ): Cell() {
            override fun equals(other: Any?): Boolean {
                return other is Option
                        && filterId == other.filterId
                        && label == other.label
            }

            override fun hashCode(): Int {
                var result = filterId.hashCode()
                result = 31 * result + label.hashCode()
                result = 31 * result + isActive.hashCode()
                return result
            }
        }
    }
}
