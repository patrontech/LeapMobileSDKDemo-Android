package com.greencopper.testmocks.interfacekit

import com.greencopper.interfacekit.filtering.*
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarData
import com.greencopper.interfacekit.filtering.filterselector.FilterSelectorData
import com.greencopper.interfacekit.navigation.layout.Layout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public class MockFilteringHandler(
    public var currentModeValue: FilteringHandler.Mode = FilteringHandler.Mode.DEFAULT,
    public var mockedCurrentStateToInfo: FilteringInfo? = null,
    public var mockedCurrentStatesMap: Map<FilteringHandler.Mode, FilteringInfo?> = emptyMap(),
    _mockedPredicate: FilteringPredicate.FilteringPredicateComputed? = null,
    public var mockedFilters: Map<FilterId, List<FilterSelectorData.Cell>> = emptyMap(),
    public var mockedOnClearTap: (filters: List<FilterId>) -> Unit = {},
    public var mockedFilteringBarData: FilteringBarData = FilteringBarData(emptyList()),
): FilteringHandler {
    override val currentMode: FilteringHandler.Mode
        get() = currentModeValue

    public var mockedPredicate: FilteringPredicate.FilteringPredicateComputed?
        get() = predicateStateFlow.value
        set(value) {
            predicateStateFlow.value = value
        }
    private val predicateStateFlow = MutableStateFlow(_mockedPredicate)

    override val currentStateToInfo: FilteringInfo?
        get() = mockedCurrentStateToInfo

    override val currentStatesToInfoMap: Map<FilteringHandler.Mode, FilteringInfo?>
        get() = mockedCurrentStatesMap

    override val predicate: StateFlow<FilteringPredicate.FilteringPredicateComputed?> = predicateStateFlow

    override fun buildBarData(layout: Layout, screenName: String): FilteringBarData = mockedFilteringBarData
    override fun buildSelectorData(): FilterSelectorData {
        return FilterSelectorData(
            filters = mockedFilters,
            onClearTap = mockedOnClearTap,
        )
    }

    override fun switchMode(mode: FilteringHandler.Mode) {
        currentModeValue = mode
        mockedPredicate = mockedPredicate.copy()
    }
}
