package com.greencopper.event.scheduleItem.ui

import android.animation.AnimatorInflater
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.services.track
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.R
import com.greencopper.event.databinding.ScheduleItemItemviewBinding
import com.greencopper.event.scheduleItem.MyScheduleManager
import com.greencopper.event.scheduleItem.ScheduleItem
import com.greencopper.event.scheduleItem.data.MyScheduleEditingInfo
import com.greencopper.event.stage.Stage
import com.greencopper.event.timeSlot.TimeSlot
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.favorites.Favoriteable
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.ui.setImageFrom
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.extensions.getFormattedDateTime
import kotlinx.coroutines.CoroutineScope
import java.time.format.FormatStyle

private val timezoneProvider: TimezoneProvider by App.lazy()
private val routeController: RouteController by App.lazy()
private val localizationService: LocalizationService by App.lazy()

internal data class ScheduleItemViewData(
    override val itemId: Long,
    val name: String,
    val dayOfEvent: String?,
    val timeOfEvent: String?,
    val stage: String?,
    val stageDetailLink: String?,
    val isInMySchedule: Boolean,
    val myScheduleAnalyticsData: MyScheduleAnalytics.Data,
) : Favoriteable<Long>

internal fun ScheduleItemItemviewBinding.bind(
    scheduleItem: ScheduleItemViewData,
    origin: Layout,
    lifecycleScope: CoroutineScope,
    myScheduleEditingInfo: MyScheduleEditingInfo?,
    stageDetailIcon: String,
    timeVisible: Boolean? = null,
    isSingleItem: Boolean = false,
) {
    scheduleItemTvDayOfEvent.text = scheduleItem.dayOfEvent
    scheduleItemTvTimeOfEvent.text = scheduleItem.timeOfEvent

    val timeAvailable = scheduleItem.timeOfEvent != null
    scheduleItemTvTimeOfEvent.isVisible = timeVisible?.let {
        if(it) timeAvailable else false
    } ?: timeAvailable

    scheduleItemStage.stageTv.text = scheduleItem.stage
    scheduleItemStage.root.isVisible = scheduleItem.stage != null

    scheduleItemStage.stageMapPin.isVisible = !scheduleItem.stageDetailLink.isNullOrEmpty()
    scheduleItemStage.stageMapPin.setImageFrom(
        stageDetailIcon,
        lifecycleScope,
        hideIfUnknown = true,
        hideIfLoading = true,
    )
    scheduleItemStage.stageMapPin.setOnSafeClickListener {
        scheduleItem.stageDetailLink
            ?.let { routeController.resolveRouteLink(it, origin) }
    }
    scheduleItemStage.stageMapPin.contentDescription =
        localizationService.getString("event.activity.stage_details.accessibility_icon_label")

    val shouldShowAddRemove = if (isSingleItem) {
        myScheduleEditingInfo?.onMainScheduleItem == true
    } else {
        myScheduleEditingInfo != null
    }
    scheduleItemAddRemove.isVisible = shouldShowAddRemove

    val animation = AnimatorInflater.loadAnimator(root.context, R.animator.bounce_scale)
    animation.setTarget(scheduleItemAddRemove)

    if (shouldShowAddRemove && myScheduleEditingInfo != null) {
        scheduleItemAddRemove.setOnSafeClickListener {
            animation.start()
            val myScheduleManager: FavoritesManager<Long> = App.resolve(tag = MyScheduleManager.diKey)
            if (myScheduleManager.favoriteIds.contains(scheduleItem.itemId)) {
                myScheduleManager.removeFromFavorites(scheduleItem)
                App.track(
                    RemoveMyScheduleAnalytics(
                        scheduleItem.myScheduleAnalyticsData
                    )
                )
            } else {
                myScheduleManager.addToFavorites(scheduleItem)
                App.track(
                    AddMyScheduleAnalytics(
                        scheduleItem.myScheduleAnalyticsData
                    )
                )
            }
        }

        val buttonDetail = if (scheduleItem.isInMySchedule) myScheduleEditingInfo.remove else myScheduleEditingInfo.add
        scheduleItemAddRemove.setImageFrom(
            buttonDetail.icon,
            origin.viewLifecycleOwner.lifecycleScope,
            hideIfUnknown = true,
            hideIfLoading = true,
        )
        scheduleItemAddRemove.contentDescription = localizationService.getString(buttonDetail.accessibilityLabel)
    }
}

internal fun ScheduleItem.toItemViewData(
    screenName: String,
    timeSlot: TimeSlot?,
    stage: Stage?,
    isInMySchedule: Boolean,
    hideEndTime: Boolean,
): ScheduleItemViewData {
    val stageName = stage?.name?.let { localizationService.getString(it) }
    val dayOfEventString = getDayOfEvent(timeSlot)
    val timeOfEventString = getTimeOfEvent(timeSlot, hideEndTime)
    val localizedName = localizationService.getString(name)
    return ScheduleItemViewData(
        itemId = itemId,
        name = localizedName,
        dayOfEvent = dayOfEventString,
        timeOfEvent = timeOfEventString,
        stage = stageName,
        stageDetailLink = stage?.stageDetailLink,
        isInMySchedule = isInMySchedule,
        myScheduleAnalyticsData = MyScheduleAnalytics.Data(
            screenName = screenName,
            scheduleItemId = itemId,
            scheduleItemName = localizedName,
            scheduleItemStartDate = timeSlot?.startDate
        )
    )
}

internal fun getTimeOfEvent(timeSlot: TimeSlot?, hideEndTime: Boolean): String? {
    if (timeSlot == null || (timeSlot.startDate == null && timeSlot.endDate == null)) return null

    val startDate = timeSlot.startDate
        ?.getFormattedDateTime(null, FormatStyle.SHORT, timezoneProvider.zoneId) ?: ""
    val endDate = timeSlot.endDate?.takeUnless { hideEndTime }
        ?.let {
            " - " + it.getFormattedDateTime(null, FormatStyle.SHORT, timezoneProvider.zoneId)
        } ?: ""

    return "$startDate$endDate"
}

internal fun getDayOfEvent(timeSlot: TimeSlot?): String? {
    if (timeSlot == null) return null
    return timeSlot.dayOfEvent.getFormattedDateTime(FormatStyle.MEDIUM, null, timezoneProvider.zoneId)
}
