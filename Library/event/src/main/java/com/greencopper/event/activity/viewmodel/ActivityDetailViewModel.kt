package com.greencopper.event.activity.viewmodel

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.services.localizationService
import com.greencopper.event.activity.ContentActivity
import com.greencopper.event.activity.data.repository.ActivityRepository
import com.greencopper.event.activity.ui.viewdata.DetailViewData
import com.greencopper.event.common.DetailViewModel
import com.greencopper.event.data.TimedScheduleItem
import com.greencopper.event.data.repository.TimedScheduleItemRepository
import com.greencopper.event.scheduleItem.ui.ScheduleItemViewData
import com.greencopper.event.scheduleItem.ui.toItemViewData
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.tags.DisplayableTag
import com.greencopper.interfacekit.widgets.resolver.WidgetCollectionResolver
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.toolkit.App
import com.greencopper.toolkit.extensions.decodeHtmlString
import kotlinx.coroutines.flow.*

internal class ActivityDetailViewModel(
    private val activityRepository: ActivityRepository,
    private val timedScheduleItemRepository: TimedScheduleItemRepository,
    private val localizationService: LocalizationService,
    private val myScheduleManager: FavoritesManager<Long>,
    val myActivitiesManager: FavoritesManager<Long>,
    widgetCollectionResolver: WidgetCollectionResolver,
    widgetResolver: WidgetResolver,
) : DetailViewModel<Long>(widgetCollectionResolver, widgetResolver, myActivitiesManager) {

    suspend fun getActivity(
        screenName: String,
        activityId: Long,
        displayableTags: List<DisplayableTag>,
        hideEndTime: Boolean
    ): Flow<DetailViewData<Long>> =
        combine(
            activityRepository.getActivityById(activityId).filterNotNull(),
            timedScheduleItemRepository.getTimedScheduleItemsForActivity(activityId),
            myScheduleManager.favoriteIdsFlow,
            myActivitiesManager.favoriteIdsFlow,
        ) { activity, scheduleItems, myScheduleItemIds, _ ->
            val itemsViewData = scheduleItems.toSortedScheduleItemsViewData(screenName, myScheduleItemIds, hideEndTime)
            activity.toDetailViewData(itemsViewData, displayableTags)
        }

    private fun ContentActivity.toDetailViewData(
        itemsViewData: List<ScheduleItemViewData>,
        displayableTags: List<DisplayableTag>,
    ): DetailViewData<Long> {
        val localizedName = localizationService.getString(name)
        val localizedSubtitle = subtitle?.let { localizationService.getString(it) }
        val htmlDescription = description?.let {
            val localizedDescription = localizationService.getString(it)
            localizedDescription.decodeHtmlString()
        }

        return DetailViewData(
            itemId = itemId,
            name = localizedName,
            subtitle = localizedSubtitle,
            description = htmlDescription,
            photo = photos.firstOrNull(),
            scheduleItemList = itemsViewData,
            tags = displayableTags.filter { tags.contains(it.name) },
            widgetCollectionKey = "activity_${itemId}_detail_primary"
        )
    }

    private fun List<TimedScheduleItem>.toSortedScheduleItemsViewData(
        screenName: String,
        myScheduleItemIds: Set<Long>,
        hideEndTime: Boolean,
    ): List<ScheduleItemViewData> {
        return sortedBy { it.timeSlot }
            .map { timedScheduleItem ->
                val scheduleItem = timedScheduleItem.scheduleItem
                val isInMySchedule = myScheduleItemIds.contains(scheduleItem.itemId)
                scheduleItem.toItemViewData(
                    screenName,
                    timedScheduleItem.timeSlot,
                    timedScheduleItem.stage,
                    isInMySchedule,
                    hideEndTime,
                )
            }
    }

    suspend fun getActivityDefaultName(activityId: Long): String? {
        return activityRepository.getActivityById(activityId).firstOrNull()?.name?.let {
            App.localizationService().getDefaultLocaleString(it)
        }
    }
}
