package com.greencopper.event.scheduleItem.viewmodel

import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.content.serializers.ZonedDateTimeWithInstantSerializer
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.R
import com.greencopper.event.data.TimedScheduleItem
import com.greencopper.event.data.repository.TimedScheduleItemRepository
import com.greencopper.event.recipe.EventConfigurationHolder
import com.greencopper.event.reminders.ui.ReminderUIManager
import com.greencopper.event.scheduleItem.ScheduleLayoutData
import com.greencopper.event.scheduleItem.viewmodel.ScheduleAction.*
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.favorites.toFavoriteable
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.filtering.QueryPattern
import com.greencopper.interfacekit.interests.recipe.InterestsConfigurationHolder
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.topbar.TopBarState
import com.greencopper.interfacekit.ui.views.navigationcontrols.KibaToolbar
import com.greencopper.interfacekit.utils.withMultipleFlowsEffect
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.*
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

internal class ScheduleReducer(
    private val timedScheduleItemRepository: TimedScheduleItemRepository,
    private val myScheduleManager: FavoritesManager<Long>,
    private val localizationService: LocalizationService,
    private val filteringHandler: FilteringHandler,
    private val eventConfigHolder: EventConfigurationHolder,
    private val interestsConfigHolder: InterestsConfigurationHolder,
    val timezoneProvider: TimezoneProvider,
    private val widgetResolver: WidgetResolver,
    private val scheduleData: ScheduleLayoutData,
    private val routeController: RouteController,
    private val reminderUIManager: ReminderUIManager,
    private val localStorage: LocalStorage,
    private val conditionChecker: ConditionChecker,
    private val json: Json,
) : Reducer<ScheduleState, ScheduleAction> {

    private var initialized = false

    override fun reduce(state: ScheduleState, action: ScheduleAction): ReduceResult<ScheduleState, ScheduleAction> {
        return when (action) {
            is ScreenLoaded -> {
                if (initialized) {
                    state.copy(
                        layout = action.layout,
                    ).withoutEffect()
                } else {
                    initialized = true

                    val newState = if (state.header.topBar == null) {
                        val topBarButtons = mutableListOf<TopBarState.TopBarButton<ScheduleAction>>()

                        eventConfigHolder.currentConfiguration.value?.reminders?.topBarIcon?.let { icon ->
                            topBarButtons.add(
                                TopBarState.TopBarButton(
                                    title = null,
                                    icon = icon,
                                    iconResource = null,
                                    side = KibaToolbar.Side.RIGHT,
                                    accessibilityLabel = "event.schedule.reminders_icon.accessibility_label",
                                    onClick = User.TappedScheduleReminders
                                )
                            )
                        }

                        scheduleData.editMyInterests?.let {
                            topBarButtons.add(
                                TopBarState.TopBarButton(
                                    title = null,
                                    icon = null,
                                    iconResource = R.drawable.ic_interests_edit,
                                    side = KibaToolbar.Side.RIGHT,
                                    accessibilityLabel = "event.schedule.edit_my_interests.accessibility_label",
                                    onClick = User.TappedEditMyInterests
                                )
                            )
                        }

                        scheduleData.search?.let {
                            topBarButtons.add(
                                TopBarState.TopBarButton(
                                    title = null,
                                    icon = null,
                                    iconResource = R.drawable.ic_search,
                                    side = KibaToolbar.Side.RIGHT,
                                    accessibilityLabel = "common.search",
                                    onClick = User.TappedSearch
                                )
                            )
                        }

                        val header: ViewState.HeaderState = state.header.copy(
                            topBar = TopBarState(topBarButtons),
                        )

                        val selectedSchedule = mutableSetOf<SelectedSchedule>().apply {
                            if (scheduleData.myFavorites?.activeOnLoad == true) {
                                add(SelectedSchedule.MySchedule)
                            }

                            if (scheduleData.myInterests?.activeOnLoad == true) {
                                add(SelectedSchedule.MyInterests)
                            }
                        }

                        val selectedView = scheduleData.defaultUI

                        state.copy(
                            layout = action.layout,
                            header = header,
                            selectedView = selectedView,
                            selectedSchedule = selectedSchedule
                        )
                    } else {
                        state
                    }

                    newState.withMultipleFlowsEffect(
                        flowOf(LoadItems),
                        myScheduleManager.favoriteIdsFlow.map { MyScheduleIdsUpdated(it) },
                        localStorage.project.interfaceKit.interestIds.state.map { InterestsUpdated(it) }
                    )
                }.also { newState ->
                    updateConditionCheckerMetadata(newState.state.isInMyInterests)
                }
            }

            is User.TappedMySchedule -> {
                state.selectedSchedule.run {
                    if (action.selected) {
                        plus(SelectedSchedule.MySchedule)
                    } else {
                        minus(SelectedSchedule.MySchedule)
                    }
                }.let {
                    state.copy(selectedSchedule = it).also { state ->
                        filteringHandler.switchMode(
                            if (state.isInMySchedule) {
                                FilteringHandler.Mode.MY_FAVORITES
                            } else {
                                FilteringHandler.Mode.DEFAULT
                            }
                        )
                    }
                }.withoutEffect()
            }

            is User.TappedMyInterests -> {
                state.selectedSchedule.run {
                    if (action.selected) {
                        plus(SelectedSchedule.MyInterests)
                    } else {
                        minus(SelectedSchedule.MyInterests)
                    }.also {
                        updateConditionCheckerMetadata(action.selected)
                    }
                }.let {
                    state.copy(selectedSchedule = it)
                }.withEffect(LoadItems)
            }

            is User.TappedAddToMySchedule -> {
                state.also {
                    myScheduleManager.addToFavorites(action.scheduleItemId.toFavoriteable())
                }.withoutEffect()
            }

            is User.TappedRemoveFromMySchedule -> {
                state.also {
                    myScheduleManager.removeFromFavorites(action.scheduleItemId.toFavoriteable())
                }.withoutEffect()
            }

            is User.TappedDay -> {
                updateSelectedDay(state, action.date)
                    .withEffect(LoadContent(purge = true))
            }

            is User.TappedNext -> {
                updateSelectedDay(state, action.date)
                    .withEffect(LoadContent(purge = true))
            }

            is User.TappedScheduleItem -> {
                state.also {
                    val layout = state.layout ?: return@also
                    routeController.resolveRouteLink(
                        scheduleData.onScheduleItemTap,
                        layout,
                        mapOf("scheduleItemId" to action.scheduleItemId.toString()),
                    )
                }.withoutEffect()
            }

            is User.TappedScheduleReminders -> {
                state.also {
                    val layout = state.layout ?: return@also
                    reminderUIManager.showReminderUI(layout)
                }.withoutEffect()
            }

            is User.TappedSearch -> {
                state.also {
                    val layout = state.layout ?: return@also
                    val search = scheduleData.search ?: return@also

                    routeController.resolveRouteLink(search.onTapRouteLink, layout)
                }.withoutEffect()
            }

            is User.TappedEditMyInterests -> {
                state.also {
                    val layout = state.layout ?: return@also
                    val routeLink = scheduleData.editMyInterests?.onTap ?: return@also

                    routeController.resolveRouteLink(routeLink, layout)
                }.withoutEffect()
            }

            is User.ViewSelected -> {
                state.copy(selectedView = action.selectedView).run {
                    if (
                        (selectedView == SelectedView.List && list != null)
                        || (selectedView == SelectedView.Timeline && timeline != null)
                    ) {
                        withoutEffect()
                    } else {
                        withEffect(LoadContent(purge = false))
                    }
                }
            }

            is MyScheduleIdsUpdated -> {
                if (state.isInMySchedule) {
                    state.withEffect(LoadItems)
                } else {
                    updateMyScheduleItems(state, action.ids)
                        .withoutEffect()
                }
            }

            is InterestsUpdated -> {
                if (state.isInMyInterests) {
                    state.withEffect(LoadItems)
                } else {
                    state.withoutEffect()
                }
            }

            is LoadItems -> {
                state.withSuspendEffect(id = "datePicker") {
                    val items =
                        try {
                            timedScheduleItemRepository.getTimedScheduleItemsForTags(
                                getTagsSqlQueryPattern(state.isInMyInterests)
                            ).first()
                                .filterIsInMySchedule(state.isInMySchedule)
                                .filterOutPastDates(state.isInMySchedule)
                                .map {
                                    it.copy(
                                        timeSlot = it.timeSlot.copy(
                                            dayOfEvent = it.timeSlot.dayOfEvent.withZoneSameInstant(
                                                timezoneProvider.zoneId
                                            )
                                        )
                                    )
                                }
                        } catch (e: LoadItemsException) {
                            emptyList()
                        }

                    LoadedItemsFromDatabase(items)
                }
            }

            is LoadedItemsFromDatabase -> {
                state.copy(items = action.items)
                    .withEffect(LoadDatePicker)
            }

            is LoadDatePicker -> {
                loadDatePicker(state, scheduleData)
                    .withEffect(LoadContent(purge = true))
            }

            is LoadContent -> {
                loadContent(
                    state = state,
                    purge = action.purge,
                    scheduleData = scheduleData,
                    favoriteIds = myScheduleManager.favoriteIds,
                    widgetCollections = getWidgetItems(scheduleData.widgetCollections),
                    timezoneProvider = timezoneProvider,
                    localizationService = localizationService,
                    hasInterests = localStorage.project.interfaceKit.interestIds.value.isNotEmpty(),
                ).withoutEffect()
            }
        }
    }

    private fun updateConditionCheckerMetadata(
        isMyInterestsSelected: Boolean,
    ) {
        conditionChecker.metadata.value = json.encodeToJsonElement(mapOf("myInterests" to isMyInterestsSelected))
    }

    private fun getTagsSqlQueryPattern(isInMyInterests: Boolean): QueryPattern? {
        val filteringSqlQuery = filteringHandler.predicate.replayCache.firstOrNull()?.toSQL()
        val interests = interestsConfigHolder.currentConfiguration.value?.interests

        val interestsSqlQuery = if (isInMyInterests) {
            val query = StringBuilder()

            localStorage.project.interfaceKit.interestIds.value.mapNotNull { interestId ->
                interests?.firstOrNull { it.id == interestId }?.tags
            }.flatten().forEach {
                if (query.isNotEmpty()) {
                    query.append(" OR ")
                }
                query.append("tags LIKE '%\"$it\"%'")
            }
            query.toString().takeIf { it.isNotEmpty() } ?: throw LoadItemsException("No interests found")
        } else null

        return filteringSqlQuery?.let {
            if (interestsSqlQuery != null) {
                "$it AND ($interestsSqlQuery)"
            } else {
                it
            }
        } ?: interestsSqlQuery
    }

    private fun List<TimedScheduleItem>.filterIsInMySchedule(isInMySchedule: Boolean) =
        if (isInMySchedule) {
            val myScheduleItemIds = myScheduleManager.favoriteIds
            filter { timedItem ->
                myScheduleItemIds.contains(timedItem.scheduleItem.itemId)
            }
        } else this

    private fun List<TimedScheduleItem>.filterOutPastDates(isInMySchedule: Boolean): List<TimedScheduleItem> {
        return if (
            scheduleData.displayMode == DisplayMode.MONTHLY
            || shouldHideDatePicker(isInMySchedule, scheduleData)
        ) {
            val now = ZonedDateTime.now(timezoneProvider.zoneId).truncatedTo(ChronoUnit.DAYS)
            filter {
                it.timeSlot.dayOfEvent.truncatedTo(ChronoUnit.DAYS) >= now
            }
        } else this
    }

    private var resolvedWidgetItems: LinkedHashMap<Int, List<WidgetCollectionView.WidgetItem>>? = null

    private fun getWidgetItems(widgetCollections: List<WidgetCollectionCellLayoutData>?): LinkedHashMap<Int, List<WidgetCollectionView.WidgetItem>> {
        return resolvedWidgetItems ?: widgetCollections?.toWidgetItemsBySortedIndex(widgetResolver)
            .also {
                resolvedWidgetItems = it
            } ?: linkedMapOf()
    }
}

@Serializable
internal sealed class ScheduleAction {
    object User {
        @Serializable
        data class TappedScheduleItem(val scheduleItemId: Long) : ScheduleAction()

        @Serializable
        data class TappedDay(val date: @Serializable(with = ZonedDateTimeWithInstantSerializer::class) ZonedDateTime) :
            ScheduleAction()

        @Serializable
        data class TappedNext(val date: @Serializable(with = ZonedDateTimeWithInstantSerializer::class) ZonedDateTime) :
            ScheduleAction()

        @Serializable
        data class TappedMySchedule(val selected: Boolean) : ScheduleAction()

        @Serializable
        data class TappedMyInterests(val selected: Boolean) : ScheduleAction()

        @Serializable
        data class ViewSelected(val selectedView: SelectedView) : ScheduleAction()

        @Serializable
        data class TappedAddToMySchedule(
            val scheduleItemId: Long,
            val itemName: String,
            val startDateTime: @Serializable(with = ZonedDateTimeWithInstantSerializer::class) ZonedDateTime?,
        ) : ScheduleAction()

        @Serializable
        data class TappedRemoveFromMySchedule(
            val scheduleItemId: Long,
            val itemName: String,
            val startDateTime: @Serializable(with = ZonedDateTimeWithInstantSerializer::class) ZonedDateTime?,
        ) : ScheduleAction()

        @Serializable
        data object TappedSearch : ScheduleAction()

        @Serializable
        data object TappedEditMyInterests : ScheduleAction()

        @Serializable
        data object TappedScheduleReminders : ScheduleAction()
    }

    @Serializable
    data class MyScheduleIdsUpdated(val ids: Set<Long>) : ScheduleAction()

    @Serializable
    data class InterestsUpdated(val interests: Set<String>) : ScheduleAction()

    @Serializable
    data object LoadItems : ScheduleAction()

    @Serializable
    data class LoadedItemsFromDatabase(val items: List<TimedScheduleItem>) : ScheduleAction()

    @Serializable
    data object LoadDatePicker : ScheduleAction()

    @Serializable
    data class LoadContent(val purge: Boolean) : ScheduleAction()
    data class ScreenLoaded(val layout: Layout) : ScheduleAction()
}

private class LoadItemsException(msg: String) : Exception(msg)
