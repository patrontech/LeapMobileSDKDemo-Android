package com.greencopper.event.scheduleItem.ui

import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.event.metrics.addToMySchedule
import com.greencopper.event.metrics.removeFromMySchedule
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

public data class AddMyScheduleAnalytics(
    val data: Data,
) : MyScheduleAnalytics(data) {
    override val eventName: EventName = EventName.addToMySchedule()
}

public data class RemoveMyScheduleAnalytics(
    val data: Data,
) : MyScheduleAnalytics(data) {
    override val eventName: EventName = EventName.removeFromMySchedule()
}

public abstract class MyScheduleAnalytics(
    private val data: Data,
) : MappedMetrics {
    protected abstract val eventName: EventName

    override fun track(provider: MappedProvider) {

        var itemName = data.scheduleItemName
        data.scheduleItemStartDate?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)?.let {
            itemName = itemName.plus("|$it")
        }

        val parameters = mapOf(
            EventParameter.screenName to data.screenName,
            EventParameter.itemId to data.scheduleItemId.toString(),
            EventParameter.itemName to itemName
        )

        provider.track(
            eventName,
            parameters
        )
    }

    public data class Data(
        val screenName: String,
        val scheduleItemId: Long,
        val scheduleItemName: String,
        val scheduleItemStartDate: ZonedDateTime?,
    )
}
