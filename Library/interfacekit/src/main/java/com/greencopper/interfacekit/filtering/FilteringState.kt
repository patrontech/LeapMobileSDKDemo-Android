package com.greencopper.interfacekit.filtering

internal typealias Filters = Map<FilterId, FilteringState.Filter>

internal data class FilteringState(
    val predicate: FilteringPredicate,
    val filters: Filters
) {

    constructor(info: FilteringInfo) : this(
        info.predicate,
        info.filters.mapValues { Filter.build(it.value) })

    sealed class Filter{
        abstract val predicate: FilteringPredicate?
        var index: Int = 0

        companion object {
            fun build(filterInfo: FilterInfo): Filter = when (filterInfo) {
                is FilterInfo.CheckBox -> CheckBox(
                    filterInfo.label,
                    filterInfo.operator,
                    filterInfo.options.map { CheckBox.Option(it) }
                ).apply {
                    index = filterInfo.index
                }
            }
        }

        data class CheckBox(
            val label: String,
            val operator: FilteringPredicate.Operator,
            val options: List<Option>
        ) : Filter() {

            override val predicate: FilteringPredicate?
                get() = options.mapNotNull {
                    if (it.isActive)
                        it.predicate
                    else null
                }.fold(null) { partialResult: FilteringPredicate?, predicate ->
                    partialResult?.let {
                        FilteringPredicate.Logic(it, operator, predicate)
                    } ?: predicate
                }

            class Option(
                val label: String,
                val predicate: FilteringPredicate,
                var isActive: Boolean = false
            ) {
                constructor(option: FilterInfo.CheckBox.Option) : this(
                    option.label,
                    option.predicate,
                    option.isActive
                )

                override fun equals(other: Any?): Boolean {
                    return other is Option
                            && label == other.label
                            && predicate == other.predicate
                            && isActive == other.isActive
                }

                override fun hashCode(): Int {
                    var result = label.hashCode()
                    result = 31 * result + predicate.hashCode()
                    result = 31 * result + isActive.hashCode()
                    return result
                }
            }

        }
    }

    fun toInfo(): FilteringInfo = FilteringInfo(this)

    data class Update(val filterId: FilterId, val action: Action) {
        sealed class Action {
            data class ToggleCheckBoxOption(val option: Filter.CheckBox.Option) : Action()
            object Clear : Action()
        }
    }
}
