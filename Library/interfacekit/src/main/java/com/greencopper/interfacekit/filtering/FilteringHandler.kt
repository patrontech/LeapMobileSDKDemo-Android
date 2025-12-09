package com.greencopper.interfacekit.filtering

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.services.track
import com.greencopper.interfacekit.filtering.FilteringHandler.Mode
import com.greencopper.interfacekit.filtering.FilteringHandler.Mode.DEFAULT
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarData
import com.greencopper.interfacekit.filtering.filterselector.FilterSelectorData
import com.greencopper.interfacekit.filtering.filterselector.ui.FilterOptionTapEventAnalytics
import com.greencopper.interfacekit.filtering.filterselector.ui.FilterSelector
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

internal typealias StateUpdate = (FilteringState.Update.Action) -> Unit

public interface FilteringHandler {
    public val currentMode: Mode

    @Deprecated(
        message = "This function only returns informations of the current FilterMode. We rather want to save all the modes and their respective infos",
        replaceWith = ReplaceWith("currentStatesToInfoMap")
    )
    public val currentStateToInfo: FilteringInfo?
    public val currentStatesToInfoMap: Map<Mode, FilteringInfo?>
    public val predicate: SharedFlow<FilteringPredicate.FilteringPredicateComputed?>
    public fun buildBarData(layout: Layout, screenName: String): FilteringBarData
    public fun buildSelectorData(): FilterSelectorData
    public fun switchMode(mode: Mode)

    @Serializable
    public enum class Mode(public val key: String) {
        DEFAULT("default"),
        MY_FAVORITES("myFavorites")
    }
}

internal interface MutableFilteringHandler : FilteringHandler {
    val states: Map<Mode, FilteringState?>
    val currentState: FilteringState?
    val selectorDataState: StateFlow<FilterSelectorData>
    fun update(stateUpdate: FilteringState.Update)
}

public typealias FilterId = String

internal class ConcreteFilteringHandler(
    override var currentMode: Mode = DEFAULT,
    filteringInfoMap: Map<Mode, FilteringInfo?>
) : MutableFilteringHandler {

    private val localizationService: LocalizationService by App.lazy()

    @Deprecated(
        message = "This function only returns informations of the current FilterMode. We rather want to save all the modes and their respective infos",
        replaceWith = ReplaceWith("currentStatesToInfoMap")
    )
    override val currentStateToInfo: FilteringInfo?
        get() = currentState?.toInfo()

    override val currentStatesToInfoMap: Map<Mode, FilteringInfo?>
        get() = states.mapValues { it.value?.toInfo() }

    override val currentState: FilteringState?
        get() = states[currentMode]

    override val states: Map<Mode, FilteringState?> =
        filteringInfoMap.mapValues { it.value?.let { FilteringState(it) } }

    private var screenName: String? = null

    @Transient
    private val mutablePredicate: MutableSharedFlow<FilteringPredicate.FilteringPredicateComputed?> by lazy {
        MutableSharedFlow<FilteringPredicate.FilteringPredicateComputed?>(replay = 1).apply {
            tryEmit(currentState?.let {
                it.predicate.query(it.filters)
            })
        }
    }

    @Transient
    override val predicate: SharedFlow<FilteringPredicate.FilteringPredicateComputed?> by lazy { mutablePredicate }

    @Transient
    private val mutableSelectorDataState: MutableStateFlow<FilterSelectorData> by lazy {
        MutableStateFlow(
            buildSelectorData()
        )
    }

    @Transient
    override val selectorDataState: StateFlow<FilterSelectorData> by lazy { mutableSelectorDataState }

    override fun switchMode(mode: Mode) {
        currentMode = mode
        mutablePredicate.tryEmit(currentState?.let { it.predicate.query(it.filters) })
        mutableSelectorDataState.value = buildSelectorData()
    }

    override fun update(stateUpdate: FilteringState.Update) {
        when (val action = stateUpdate.action) {
            is FilteringState.Update.Action.ToggleCheckBoxOption -> {
                toggleCheckBoxOption(stateUpdate.filterId, action.option)
            }
            FilteringState.Update.Action.Clear -> {
                clear(stateUpdate.filterId)
            }
        }

        currentState?.let {
            mutablePredicate.tryEmit(it.predicate.query(it.filters))
        }
        mutableSelectorDataState.value = buildSelectorData()
    }

    override fun buildBarData(layout: Layout, screenName: String): FilteringBarData =
        currentState?.filters?.takeUnless { it.isEmpty() }?.let { filters ->
            FilteringBarData(filters.map {
                buildFilteringBarDataFilter(
                    id = it.key, filter = it.value, handler = this, layout = layout
                )
            }.sortedBy { it.index })
        } ?: FilteringBarData(emptyList())

    override fun buildSelectorData(): FilterSelectorData {
        val filters = currentState?.filters ?: emptyMap()
        return FilterSelectorData(filters.mapValues {
            buildFilterSelectorDataCell(it.key, it.value)
        }) {
            it.forEach { filterId ->
                update(FilteringState.Update(filterId, FilteringState.Update.Action.Clear))
            }
        }
    }

    private fun buildFilteringBarDataFilter(
        id: FilterId,
        filter: FilteringState.Filter,
        handler: MutableFilteringHandler,
        layout: Layout
    ): FilteringBarData.Filter {
        return when (filter) {
            is FilteringState.Filter.CheckBox -> {
                val activeBarLabel = filter.getActiveBarLabel()
                FilteringBarData.Filter(
                    id = id,
                    index = filter.index,
                    name = activeBarLabel ?: localizationService.getString(filter.label),
                    showArrow = true,
                    isSelected = activeBarLabel != null
                ) {
                    layout.context?.let {
                        FilterSelector(it).apply {
                            setup(handler.selectorDataState, listOf(id))
                            show()
                        }
                    }
                }
            }
        }
    }

    private fun FilteringState.Filter.CheckBox.getActiveBarLabel(): String? {
        val activeOptionLabels =
            options.mapNotNull { if (it.isActive) localizationService.getString(it.label) else null }
        return with(activeOptionLabels.size) {
            when {
                this in 1..2 -> activeOptionLabels.joinToString(separator = ", ")
                this >= 3 -> "${localizationService.getString(label)} ($this)"
                else -> null
            }
        }
    }

    private fun FilteringState.Filter.CheckBox.selectorData(
        filterId: FilterId, stateUpdate: StateUpdate
    ): List<FilterSelectorData.Cell> {
        return listOf(FilterSelectorData.Cell.Title(label)).plus(options.map {
            FilterSelectorData.Cell.Option(
                filterId, it.label, it.isActive
            ) {
                stateUpdate(FilteringState.Update.Action.ToggleCheckBoxOption(it))
            }
        })

    }

    private fun toggleCheckBoxOption(
        filterId: FilterId, option: FilteringState.Filter.CheckBox.Option
    ) {
        val filter =
            currentState?.filters?.get(filterId) as? FilteringState.Filter.CheckBox ?: return
        val matchingOption = filter.options.firstOrNull { it == option } ?: return

        matchingOption.isActive = !option.isActive

        if (matchingOption.isActive) {
            App.track(
                FilterOptionTapEventAnalytics(
                    localizationService.getDefaultLocaleString(matchingOption.label),
                    localizationService.getDefaultLocaleString(filter.label),
                    filterId,
                    screenName
                )
            )
        }
    }

    private fun clear(filterId: FilterId) {
        currentState?.filters?.get(filterId)?.let { filter ->
            when (filter) {
                is FilteringState.Filter.CheckBox -> filter.options.forEach {
                    it.isActive = false
                }
            }
        }
    }

    private fun buildFilterSelectorDataCell(
        filterId: FilterId, filter: FilteringState.Filter
    ): List<FilterSelectorData.Cell> {
        val stateUpdate: StateUpdate = {
            update(FilteringState.Update(filterId, it))
        }

        return when (filter) {
            is FilteringState.Filter.CheckBox -> filter.selectorData(filterId, stateUpdate)
        }
    }
}
