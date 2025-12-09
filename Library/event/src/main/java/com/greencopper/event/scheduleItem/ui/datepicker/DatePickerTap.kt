package com.greencopper.event.scheduleItem.ui.datepicker

import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal class DatePickerTap(private val scheduleScreenName: String, private val dateClicked: ZonedDateTime) :
    MappedMetrics {
    override fun track(provider: MappedProvider) {
        val parameters = mapOf(
            EventParameter.screenName to scheduleScreenName,
            EventParameter.itemName to dateClicked.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        )
        provider.track(EventName("schedule/day_picker_tap"), parameters)
    }
}
