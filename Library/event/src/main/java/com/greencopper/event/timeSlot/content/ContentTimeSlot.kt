package com.greencopper.event.timeSlot.content

import com.greencopper.event.timeSlot.data.database.TimeSlotEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ContentTimeSlot(
    val id: Long,
    val scheduleItemId: Long,
    @SerialName("dayOfEvent") val dayOfEventText: String,
    @SerialName("startDate") val startDateText: String? = null,
    @SerialName("endDate") val endDateText: String? = null
)

internal fun ContentTimeSlot.toEntityModel() =
    TimeSlotEntity(id, scheduleItemId, dayOfEventText, startDateText, endDateText)