package com.greencopper.event.scheduleItem.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.putKiboSerializable
import com.greencopper.core.localization.service.*
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.scheduleItem.ScheduleLayoutData
import com.greencopper.event.scheduleItem.ui.getTimeOfEvent
import com.greencopper.event.scheduleItem.viewmodel.ScheduleAction.*
import com.greencopper.event.scheduleItem.viewmodel.SelectedView.List
import com.greencopper.event.scheduleItem.viewmodel.SelectedView.Timeline
import com.greencopper.event.timeSlot.TimeSlot
import com.greencopper.interfacekit.favorites.Favoriteable
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarData
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarState
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarCell
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.topbar.TopBarState
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.toggl.komposable.architecture.Store
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import kotlin.math.max

internal class ScheduleListViewModel(
    private val localizationService: LocalizationService,
    private val filteringHandler: FilteringHandler,
    val timezoneProvider: TimezoneProvider,
    private val scheduleData: ScheduleLayoutData,
    private val store: Store<ScheduleState, ScheduleAction>,
    private val scope: CoroutineScope,
    val conditionChecker: ConditionChecker,
) : ViewModel() {

    internal var filteringUpdater: Flow<Unit> =
        filteringHandler.predicate.shareIn(scope, SharingStarted.Eagerly).map { }

    init {
        scope.launch {
            filteringUpdater.collectLatest {
                store.send(LoadItems)
            }
        }
    }

    internal suspend fun getInitialSetup(layout: Layout): TopBarState<ScheduleAction> {
        store.send(ScreenLoaded(layout))

        return store.state.map { it.header.topBar }.filterNotNull().first()
    }

    internal fun getFilteringBarButtons(): List<FilteringBarState.ButtonState> {
        val buttons = mutableListOf<FilteringBarState.ButtonState>()
        val state = runBlocking { store.state.first() }
        scheduleData.myFavorites?.filteringButton?.let { filteringButton ->
            val defaultState = FilteringBarCell.ButtonState.State(
                filteringButton.unselected.title,
                filteringButton.unselected.icon,
                localizationService.getString(filteringButton.unselected.accessibilityLabel)
            )
            val selectedState = FilteringBarCell.ButtonState.State(
                filteringButton.selected.title,
                filteringButton.selected.icon,
                localizationService.getString(filteringButton.selected.accessibilityLabel)
            )

            buttons.add(
                FilteringBarState.ButtonState(
                    defaultState,
                    selectedState,
                    state.isInMySchedule,
                ) { filterMySchedule ->
                    store.send(
                        User.TappedMySchedule(filterMySchedule)
                    )
                }
            )
        }

        scheduleData.myInterests?.filteringButton?.let { filteringButton ->
            val defaultState = FilteringBarCell.ButtonState.State(
                filteringButton.unselected.title,
                filteringButton.unselected.icon,
                localizationService.getString(filteringButton.unselected.accessibilityLabel)
            )
            val selectedState = FilteringBarCell.ButtonState.State(
                filteringButton.selected.title,
                filteringButton.selected.icon,
                localizationService.getString(filteringButton.selected.accessibilityLabel)
            )

            buttons.add(
                FilteringBarState.ButtonState(
                    defaultState,
                    selectedState,
                    state.isInMyInterests,
                ) { filterMyInterests ->
                    store.send(
                        User.TappedMyInterests(filterMyInterests)
                    )
                }
            )
        }

        scheduleData.timeline?.takeIf { it.displayToggle }?.let {
            val title = localizationService.getStringOrDefault(
                "event.schedule.timeline.toggle",
                "Timeline"
            )
            val buttonState = FilteringBarCell.ButtonState.State(null, it.buttonIcon, title)
            buttons.add(
                FilteringBarState.ButtonState(
                    buttonState,
                    buttonState.copy(),
                    state.selectedView == Timeline,
                ) { isChecked ->
                    store.send(
                        User.ViewSelected(
                            if (isChecked) {
                                Timeline
                            } else {
                                List
                            }
                        )
                    )
                })
        }

        return buttons
    }

    val datePickerState: StateFlow<ViewState.DatePickerState?> = store.state.map { state ->
        state.header.datePicker
    }.stateIn(scope, SharingStarted.Lazily, null)

    val listState: StateFlow<ViewState.ListState.Content?> = store.state.map { state ->
        state.list.takeIf { state.selectedView == List } as? ViewState.ListState.Content
    }.stateIn(scope, SharingStarted.Lazily, null)

    val timelineState = store.state.map { state ->
        state.timeline.takeIf { state.selectedView == Timeline } as? ViewState.TimelineState.Content
    }.stateIn(scope, SharingStarted.Lazily, null)

    val emptyState = store.state.map { state ->
        when (state.selectedView) {
            List -> state.list as? ViewState.ListState.Empty
            Timeline -> state.timeline as? ViewState.TimelineState.Empty
        }
    }.stateIn(scope, SharingStarted.Lazily, null)

    val selectedSchedule = store.state.map { state ->
        state.selectedSchedule
    }.shareIn(scope, SharingStarted.Lazily).distinctUntilChanged()

    internal fun onDatePickerDateTap(selectedDate: ZonedDateTime) {
        store.send(User.TappedDay(selectedDate))
    }

    internal fun onScheduleItemTap(itemId: Long) {
        store.send(User.TappedScheduleItem(itemId))
    }

    internal fun onNextDateTap(nextDate: ZonedDateTime) {
        store.send(User.TappedNext(nextDate))
    }

    internal fun sendAction(action: ScheduleAction) {
        store.send(action)
    }

    internal fun onAddRemoveFavoritesTap(
        isInMySchedule: Boolean,
        itemId: Long,
        name: String,
        startDate: ZonedDateTime?,
    ) {
        if (isInMySchedule) {
            store.send(
                User.TappedRemoveFromMySchedule(
                    itemId,
                    name,
                    startDate
                )
            )
        } else {
            store.send(
                User.TappedAddToMySchedule(
                    itemId,
                    name,
                    startDate
                )
            )
        }
    }

    internal fun saveState(outState: Bundle, key: String) {
        val state = runBlocking {
            store.state.first()
        }
        outState.putKiboSerializable(key, state)
    }

    internal fun findNextScheduledItemPosition(items: List<ScheduleListViewData>): Int {
        val now = ZonedDateTime.now()
        return max(0, items.indexOfFirst {
            (it as? ScheduleListViewData.ScheduleItem)?.timeSlot?.let { timeSlot ->
                timeSlot.startDate?.withZoneSameLocal(now.zone)?.isAfter(now) ?: false
            } ?: false
        } - 1)
    }

    fun getFilteringBarData(layout: Layout): FilteringBarData =
        filteringHandler.buildBarData(layout, scheduleData.analytics.screenName)

    //TODO Use [filteringHandler.currentStatesToInfoMap]
    fun getCurrentFilterState(): SavedFiltering =
        SavedFiltering(filteringHandler.currentMode, filteringHandler.currentStateToInfo)

    @Serializable
    data class SavedFiltering(val mode: FilteringHandler.Mode, val filteringInfo: FilteringInfo? = null) :
        KiboSerializable<SavedFiltering> {
        override fun getSerializer(): KSerializer<SavedFiltering> = serializer()
    }
}

internal sealed class ScheduleListViewData {

    data class ScheduleItem(
        override val itemId: Long,
        val name: String,
        val timeSlot: TimeSlot,
        val stageId: Long? = -1,
        val stageLabel: String?,
        val stageOrder: Int?,
        val photo: String?,
        val isInMySchedule: Boolean,
        val hideEndTime: Boolean,
    ) : ScheduleListViewData(), Favoriteable<Long> {
        val timeOfEvent: String? = getTimeOfEvent(timeSlot, hideEndTime)
    }

    sealed class HeaderItem(val label: String) : ScheduleListViewData() {
        data class DayHeaderItem(
            val day: String,
        ) : HeaderItem(day)

        data class TimeHeaderItem(
            val startTime: String,
        ) : HeaderItem(startTime)
    }

    data class WidgetCollectionHolder(
        val key: Int,
        val widgets: List<WidgetCollectionView.WidgetItem>,
    ) : ScheduleListViewData()

    data class NextDateButton(
        val label: String,
        val nextDate: ZonedDateTime,
    ) : ScheduleListViewData()
}
