package com.greencopper.event.scheduleItem.viewmodel

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.event.data.TimedScheduleItem
import com.greencopper.event.scheduleItem.ScheduleData
import com.greencopper.event.scheduleItem.ScheduleItem
import com.greencopper.event.scheduleItem.ScheduleLayoutData
import com.greencopper.event.stage.Stage
import com.greencopper.event.timeSlot.TimeSlot
import com.greencopper.interfacekit.favorites.FavoriteConfig
import com.greencopper.interfacekit.interests.integration.IntegratedInterestsData
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import java.time.ZonedDateTime

internal fun createScheduleData(
    reminders: ScheduleData.Reminders? = null,
    timeline: TimelineData? = null,
    search: Search? = null,
    widgetCollections: List<WidgetCollectionCellLayoutData> = emptyList(),
    myFavorites: FavoriteConfig? = null,
    myInterests: IntegratedInterestsData? = null,
    displayMode: DisplayMode = DisplayMode.DAILY,
    hideEndTime: Boolean = false,
    emptyScheduleImage: String = "emptyScheduleImage",
    screenName: String = "screenName",
    editMyInterests: ScheduleData.EditMyInterests? = null,
) = ScheduleLayoutData(
    title = "title",
    displayImages = false,
    emptyScheduleImage = emptyScheduleImage,
    onScheduleItemTap = "onScheduleItemTap",
    filtering = null,
    reminders = reminders,
    defaultUI = SelectedView.List,
    timeline = timeline,
    search = search,
    widgetCollections = widgetCollections,
    favoritesEditing = null,
    myFavorites = myFavorites,
    myInterests = myInterests,
    hideEndTime = hideEndTime,
    displayMode = displayMode,
    editMyInterests = editMyInterests,
    analytics = ScreenNameAnalytics(screenName = screenName),
    redirectionHash = RedirectionHash(
        featureKey = FeatureKey(
            name = "",
            version = 0
        ), identifier = null
    )
)

internal fun generateTimedScheduleItems(
    vararg items: TimedScheduleItemGeneratorItem,
): MutableList<TimedScheduleItem> {
    return items.mapIndexed { index, item ->
        TimedScheduleItem(
            scheduleItem = ScheduleItem(
                itemId = index.toLong(),
                activityId = index.toLong(),
                stageId = index.toLong(),
                name = item.name ?: "name$index",
                subtitle = "subtitle$index",
                description = "description$index",
                photos = listOf(),
                tags = listOf("tag$index"),
                performerIds = listOf(),
            ),
            timeSlot = TimeSlot(
                id = index.toLong(),
                scheduleItemId = index.toLong(),
                dayOfEvent = item.eventDate,
                startDate = item.startDate,
                endDate = item.endDate,
            ),
            stage = Stage(
                id = index.toLong(),
                name = "stageName$index",
                subtitle = "subtitle$index",
                photos = listOf(),
                tags = listOf(),
                stageDetailLink = "stageDetailLink",
                order = 1
            ).takeIf { item.hasStage }
        )
    }.toMutableList()
}

internal data class TimedScheduleItemGeneratorItem(
    val eventDate: ZonedDateTime,
    val startDate: ZonedDateTime? = null,
    val endDate: ZonedDateTime? = null,
    val hasStage: Boolean = false,
    val name: String? = null,
)
