package com.greencopper.event.scheduleItem.viewmodel

import com.greencopper.core.localization.service.getString
import com.greencopper.core.services.localizationService
import com.greencopper.event.activity.ui.viewdata.DetailViewData
import com.greencopper.event.common.DetailViewModel
import com.greencopper.event.scheduleItem.ScheduleItem
import com.greencopper.event.scheduleItem.data.repository.ScheduleItemRepository
import com.greencopper.event.scheduleItem.ui.ScheduleItemViewData
import com.greencopper.event.scheduleItem.ui.toItemViewData
import com.greencopper.event.stage.data.repository.StageRepository
import com.greencopper.event.timeSlot.data.repository.TimeSlotRepository
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.tags.DisplayableTag
import com.greencopper.interfacekit.widgets.resolver.WidgetCollectionResolver
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.toolkit.App
import com.greencopper.toolkit.extensions.decodeHtmlString
import kotlinx.coroutines.flow.*

internal class ScheduleItemDetailViewModel(
    private val scheduleItemRepository: ScheduleItemRepository,
    private val timeSlotRepository: TimeSlotRepository,
    private val stageRepository: StageRepository,
    private val myScheduleManager: FavoritesManager<Long>,
    widgetCollectionResolver: WidgetCollectionResolver,
    widgetResolver: WidgetResolver,
) : DetailViewModel<Long>(widgetCollectionResolver, widgetResolver, myScheduleManager) {

    suspend fun getScheduleDetailItem(
        screenName: String,
        scheduleItemId: Long,
        displayableTags: List<DisplayableTag>,
        hideEndTime: Boolean,
    ): Flow<DetailViewData<Long>> =
        combine(
            scheduleItemRepository.getScheduleItemById(scheduleItemId).filterNotNull(),
            myScheduleManager.favoriteIdsFlow
        ) { scheduleItem, myScheduleItemIds ->
            val timeSlot =
                timeSlotRepository.getTimeSlotForScheduleItem(scheduleItem.itemId).firstOrNull()
            val stage =
                scheduleItem.stageId?.let { stageRepository.getStageForId(it).firstOrNull() }
            val isInMySchedule = myScheduleItemIds.contains(scheduleItem.itemId)
            val itemViewData =
                scheduleItem.toItemViewData(screenName, timeSlot, stage, isInMySchedule, hideEndTime)

            scheduleItem.toDetailViewData(itemViewData, displayableTags)
        }

    private fun ScheduleItem.toDetailViewData(
        scheduleItem: ScheduleItemViewData,
        displayableTags: List<DisplayableTag>,
    ): DetailViewData<Long> {
        val localizationService = App.localizationService()
        val localizedName = localizationService.getString(name)
        val localizedSubtitle = subtitle?.let { localizationService.getString(it) }
        val htmlDescription = description?.let {
            val localizedDescription = localizationService.getString(it)
            localizedDescription.decodeHtmlString()
        }

        val timeSlotList = listOf(scheduleItem)
        return DetailViewData(
            itemId = itemId,
            name = localizedName,
            subtitle = localizedSubtitle,
            description = htmlDescription,
            photo = photos.firstOrNull(),
            scheduleItemList = timeSlotList,
            tags = displayableTags.filter { tags.contains(it.name) },
            widgetCollectionKey = "scheduleItem_${itemId}_detail_primary",
        )
    }

    suspend fun getScheduleItemDefaultName(scheduleItemId: Long): String? {
        return scheduleItemRepository.getScheduleItemById(scheduleItemId).firstOrNull()?.name?.let {
            App.localizationService().getDefaultLocaleString(it)
        }
    }
}
