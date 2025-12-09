package com.greencopper.event.scheduleItem.viewmodel

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.data.TimedScheduleItem
import com.greencopper.event.scheduleItem.ScheduleLayoutData
import com.greencopper.event.scheduleItem.ui.timeline.TimelineAdapter
import com.greencopper.event.scheduleItem.ui.utils.DateUtils
import com.greencopper.interfacekit.empty.EmptyPage
import com.greencopper.interfacekit.empty.EmptyState
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.greencopper.toolkit.extensions.getFormattedDateTime
import com.greencopper.toolkit.extensions.truncateToMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

internal fun loadDatePicker(
    state: ScheduleState,
    scheduleData: ScheduleLayoutData,
): ScheduleState {
    val datesByDay = state.items.map { timedItem -> timedItem.timeSlot }
        .groupBy { it.dayOfEvent }
        .keys
        .sorted()

    val datePickerDatePool = if (scheduleData.displayMode == DisplayMode.MONTHLY) {
        datesByDay.map { it.truncateToMonth() }.distinct()
    } else datesByDay

    val currentSelectedDate = state.header.datePicker?.selectedDate
    val selectedDate = if (isSelectedDayInvalid(currentSelectedDate, datePickerDatePool)) {
        DateUtils.findNearestDateOrToday(
            dateList = datesByDay,
            selectedDate = currentSelectedDate,
        ).run {
            if (scheduleData.displayMode == DisplayMode.MONTHLY) {
                truncateToMonth()
            } else this
        }
    } else currentSelectedDate

    val datePicker = selectedDate?.let {
        ViewState.DatePickerState(
            dates = datePickerDatePool.takeIf {
                !(shouldHideDatePicker(state.isInMySchedule, scheduleData)
                        || scheduleData.displayMode == DisplayMode.MONTHLY && datePickerDatePool.size == 1)
            } ?: emptyList(),
            selectedDate = selectedDate,
            displayMode = scheduleData.displayMode
        )
    }

    return state.copy(
        header = state.header.copy(
            datePicker = datePicker
        )
    )
}

internal fun loadContent(
    state: ScheduleState,
    purge: Boolean,
    scheduleData: ScheduleLayoutData,
    favoriteIds: Set<Long>,
    widgetCollections: LinkedHashMap<Int, List<WidgetCollectionView.WidgetItem>>?,
    timezoneProvider: TimezoneProvider,
    localizationService: LocalizationService,
    hasInterests: Boolean,
): ScheduleState {

    state.header.datePicker ?: return state

    //Avoid recalculation if no purge requested (i.e. selectedView changed)
    if (!purge) {
        when (state.selectedView) {
            SelectedView.List -> {
                state.list?.let { return state }
            }

            SelectedView.Timeline -> {
                state.timeline?.let { return state }
            }
        }
    }

    val filteredItems =
        state.header.datePicker.selectedDate.let { selectedDate ->
            val filterMode: ((TimedScheduleItem) -> Boolean)? = when {
                shouldHideDatePicker(state.isInMySchedule, scheduleData) -> null
                scheduleData.displayMode == DisplayMode.DAILY -> { timedItem -> timedItem.timeSlot.dayOfEvent == selectedDate }
                scheduleData.displayMode == DisplayMode.MONTHLY -> { timedItem ->
                    timedItem.timeSlot.dayOfEvent.truncateToMonth() == selectedDate
                }

                else -> null
            }
            filterMode?.let { state.items.filter(filterMode) } ?: state.items
        }.sortedWith(
            compareBy<TimedScheduleItem> { it.timeSlot.dayOfEvent }
                .thenBy(nullsFirst()) { it.timeSlot.startDate }
        )

    return when (state.selectedView) {
        SelectedView.List -> {
            state.copy(
                list = getListState(
                    filteredItems,
                    state.header.datePicker,
                    state.selectedSchedule,
                    scheduleData,
                    favoriteIds,
                    widgetCollections,
                    timezoneProvider,
                    localizationService,
                    hasInterests
                ),
                timeline = if (purge) null else state.timeline
            )
        }

        SelectedView.Timeline -> {
            state.copy(
                timeline = scheduleData.timeline?.let {
                    getTimelineState(
                        filteredItems,
                        state.selectedSchedule,
                        favoriteIds,
                        scheduleData.hideEndTime,
                        scheduleData.timeline.defaultDuration.toLong(),
                        scheduleData,
                        localizationService,
                        hasInterests
                    )
                },
                list = if (purge) null else state.list,
            )
        }
    }
}

private fun getListState(
    filteredItems: List<TimedScheduleItem>,
    datePickerState: ViewState.DatePickerState,
    selectedSchedule: Set<SelectedSchedule>,
    scheduleData: ScheduleLayoutData,
    favoriteIds: Set<Long>,
    widgetCollections: LinkedHashMap<Int, List<WidgetCollectionView.WidgetItem>>?,
    timezoneProvider: TimezoneProvider,
    localizationService: LocalizationService,
    hasInterests: Boolean,
): ViewState.ListState {

    if (filteredItems.isEmpty()) return getEmptyState(
        selectedSchedule,
        SelectedView.List,
        scheduleData,
        hasInterests,
    ) as ViewState.ListState.Empty

    val mode = when {
        shouldHideDatePicker(selectedSchedule.contains(SelectedSchedule.MySchedule), scheduleData) -> DisplayMode.MONTHLY
        else -> scheduleData.displayMode
    }

    val groupMode: (TimedScheduleItem) -> ZonedDateTime? = when (mode) {
        DisplayMode.MONTHLY -> { timedScheduleItem ->
            timedScheduleItem.timeSlot.dayOfEvent.truncatedTo(
                ChronoUnit.HOURS
            )
        }

        DisplayMode.DAILY -> { timedScheduleItem -> timedScheduleItem.timeSlot.startDate }
    }
    val groupedPerStartDate = filteredItems
        .groupBy(groupMode)

    val scheduleDataList = mutableListOf<ScheduleListViewData>().apply {
        //<editor-fold desc="Setup"> ///////////////////////
        var addedWidgetCollections = 0
        var headerCounter = 0

        val widgets = widgetCollections?.let { LinkedHashMap(widgetCollections) }
            ?: linkedMapOf()

        fun tryInsertWidgets(index: Int) {
            widgets[index]?.let {
                add(ScheduleListViewData.WidgetCollectionHolder(index, it))
                addedWidgetCollections++
                widgets.remove(index)
            }
        }

        fun calculatedIndex(): Int = size - addedWidgetCollections - headerCounter
        //</editor-fold>/ END: Setup ///////////////////////

        for ((startDate, scheduleItems) in groupedPerStartDate) {
            val index = calculatedIndex()
            tryInsertWidgets(index)

            startDate?.let {
                when (mode) {
                    DisplayMode.MONTHLY -> add(startDate.toDayHeaderViewData(timezoneProvider.zoneId))
                    DisplayMode.DAILY -> add(startDate.toTimeHeaderViewData(timezoneProvider.zoneId))
                }
                headerCounter++
            }
            scheduleItems
                .map { timedScheduleItem ->
                    timedScheduleItem.toScheduleViewData(favoriteIds, scheduleData.hideEndTime, localizationService)
                }
                .sortedWith(compareBy<ScheduleListViewData.ScheduleItem> { it.timeSlot.startDate }
                    .thenBy { it.name.lowercase() })
                .forEach { viewData ->
                    tryInsertWidgets(calculatedIndex())
                    add(viewData)
                }
        }

        widgets.forEach {
            add(ScheduleListViewData.WidgetCollectionHolder(it.key, it.value))
        }
    }

    (datePickerState.dates.indexOf(datePickerState.selectedDate))
        .takeIf { it in 0 until datePickerState.dates.size - 1 }?.let { indexNextDate ->
            scheduleDataList.add(
                createNextDateButton(
                    mode,
                    datePickerState.dates[indexNextDate + 1],
                    localizationService,
                )
            )
        }

    return ViewState.ListState.Content(scheduleDataList)
}

private fun getTimelineState(
    items: List<TimedScheduleItem>,
    selectedSchedule: Set<SelectedSchedule>,
    favoriteIds: Set<Long>,
    hideEndTime: Boolean,
    defaultDuration: Long,
    scheduleData: ScheduleLayoutData,
    localizationService: LocalizationService,
    hasInterests: Boolean,
): ViewState.TimelineState {

    val timelineItems = items.mapNotNull { item ->
        item.takeIf { item.timeSlot.startDate != null }
            ?.let {
                val startDate = item.timeSlot.startDate!!
                val endDate = item.timeSlot.endDate.takeUnless { hideEndTime }

                val computed = endDate ?: startDate.plusMinutes(defaultDuration)
                val defaultEndDate = startDate.plusMinutes(MINIMUM_TIMELINE_ITEM_MINUTES_DURATION)
                val computedEndDate = computed.takeIf {
                    it.isAfter(defaultEndDate)
                } ?: defaultEndDate

                TimelineAdapter.EventData(
                    id = item.scheduleItem.itemId,
                    name = localizationService.getString(item.scheduleItem.name),
                    stageId = item.stage?.id,
                    stageLabel = localizationService.getString(item.stage?.name),
                    stageOrder = item.stage?.order,
                    isInMySchedule = favoriteIds.contains(item.scheduleItem.itemId),
                    startDate = startDate,
                    endDate = endDate,
                    computedEndDate = computedEndDate
                )
            }
    }.sortedWith(compareBy<TimelineAdapter.EventData> { it.startDate }.thenBy { it.name })

    if (timelineItems.isEmpty()) return getEmptyState(
        selectedSchedule,
        SelectedView.Timeline,
        scheduleData,
        hasInterests,
    ) as ViewState.TimelineState.Empty

    return ViewState.TimelineState.Content(timelineItems)
}

internal const val MINIMUM_TIMELINE_ITEM_MINUTES_DURATION = 30L

internal fun shouldHideDatePicker(isInMySchedule: Boolean, scheduleData: ScheduleLayoutData): Boolean {
    return isInMySchedule && !(scheduleData.myFavorites?.showPicker ?: true)
}

private fun isSelectedDayInvalid(selectedDate: ZonedDateTime?, dates: List<ZonedDateTime>): Boolean {
    return selectedDate == null
            || dates.isNotEmpty() && (!dates.contains(selectedDate))
}

internal fun updateMyScheduleItems(state: ScheduleState, favsIds: Set<Long>): ScheduleState {
    val newListState = if (state.list is ViewState.ListState.Content) {
        ViewState.ListState.Content(state.list.items.map { scheduleListViewData ->
            if (scheduleListViewData !is ScheduleListViewData.ScheduleItem) return@map scheduleListViewData

            val favsContainsId = favsIds.contains(scheduleListViewData.itemId)
            if (
                scheduleListViewData.isInMySchedule && !favsContainsId
                || !scheduleListViewData.isInMySchedule && favsContainsId
            ) {
                scheduleListViewData.copy(isInMySchedule = !scheduleListViewData.isInMySchedule)
            } else {
                scheduleListViewData
            }
        })
    } else state.list

    val newTimelineState = if (state.timeline is ViewState.TimelineState.Content) {
        ViewState.TimelineState.Content(state.timeline.items.map { timelineItem ->
            val favsContainsId = favsIds.contains(timelineItem.id)
            val itemInMySchedule = timelineItem.isInMySchedule

            if (
                (itemInMySchedule && !favsContainsId)
                || (!itemInMySchedule && favsContainsId)
            ) {
                timelineItem.copy(isInMySchedule = !itemInMySchedule)
            } else timelineItem
        })
    } else state.timeline

    return state.copy(
        list = newListState,
        timeline = newTimelineState,
    )
}

internal fun updateSelectedDay(state: ScheduleState, selectedDate: ZonedDateTime): ScheduleState {
    return state.copy(header = state.header.copy(datePicker = state.header.datePicker?.copy(selectedDate = selectedDate)))
}

internal fun getEmptyState(
    selectedSchedule: Set<SelectedSchedule>,
    selectedView: SelectedView,
    scheduleData: ScheduleLayoutData,
    hasInterests: Boolean,
): EmptyState {

    val isInMySchedule = selectedSchedule.contains(SelectedSchedule.MySchedule)
    val isInMyInterests = selectedSchedule.contains(SelectedSchedule.MyInterests)

    val emptyPage = if (isInMySchedule) {
        if (isInMyInterests && !hasInterests) {
            scheduleData.myInterests?.emptyPage
        } else {
            scheduleData.myFavorites?.emptyPage
        }
    } else if (isInMyInterests) {
        scheduleData.myInterests?.emptyPage
    } else null

    return when (selectedView) {
        SelectedView.List -> {
            (emptyPage ?: EmptyPage(
                scheduleData.emptyScheduleImage,
                "event.schedule.fullschedule.empty.title",
                "event.schedule.fullschedule.empty.subtitle",
            )).let {
                ViewState.ListState.Empty(
                    title = it.title,
                    subtitle = it.subtitle,
                    imageName = it.image,
                    topWidgetCollection = it.topWidgetCollection,
                    screenName = scheduleData.analytics.screenName
                )
            }
        }

        SelectedView.Timeline -> {
            (emptyPage ?: EmptyPage(
                scheduleData.timeline!!.emptyStateImage,
                "event.schedule.timeline.empty.title",
                "event.schedule.timeline.empty.subtitle",
            )).let {
                ViewState.TimelineState.Empty(
                    title = it.title,
                    subtitle = it.subtitle,
                    imageName = it.image,
                    topWidgetCollection = it.topWidgetCollection,
                    screenName = scheduleData.analytics.screenName
                )
            }
        }
    }

}

private fun TimedScheduleItem.toScheduleViewData(
    favoriteIds: Set<Long>,
    hideEndTime: Boolean,
    localizationService: LocalizationService,
): ScheduleListViewData.ScheduleItem {
    var stageId: Long? = null
    var localizedStageName: String? = null
    var stageOrder: Int? = null
    stage?.let { stage ->
        stageId = stage.id
        localizedStageName = localizationService.getString(stage.name)
        stageOrder = stage.order
    }
    val localizedName = localizationService.getString(scheduleItem.name)
    val isInMySchedule = favoriteIds.contains(scheduleItem.itemId)

    return ScheduleListViewData.ScheduleItem(
        itemId = scheduleItem.itemId,
        name = localizedName,
        timeSlot = timeSlot,
        stageId = stageId,
        stageLabel = localizedStageName,
        stageOrder = stageOrder,
        photo = scheduleItem.photos.firstOrNull(),
        isInMySchedule = isInMySchedule,
        hideEndTime = hideEndTime
    )
}

private fun ZonedDateTime.toTimeHeaderViewData(zoneId: ZoneId) =
    ScheduleListViewData.HeaderItem.TimeHeaderItem(
        getFormattedDateTime(null, FormatStyle.SHORT, zoneId)
    )

private fun ZonedDateTime.toDayHeaderViewData(zoneId: ZoneId) =
    ScheduleListViewData.HeaderItem.DayHeaderItem(
        getFormattedDateTime(FormatStyle.FULL, null, zoneId)
    )

private fun createNextDateButton(
    mode: DisplayMode,
    nextDate: ZonedDateTime,
    localizationService: LocalizationService,
) =
    ScheduleListViewData.NextDateButton(
        label = when (mode) {
            DisplayMode.MONTHLY -> localizationService.getString("event.schedule.next.month")
            DisplayMode.DAILY -> localizationService.getString("event.schedule.next.day")
        },
        nextDate = nextDate
    )
