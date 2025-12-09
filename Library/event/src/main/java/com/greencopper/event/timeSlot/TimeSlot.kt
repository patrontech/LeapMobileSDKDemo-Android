package com.greencopper.event.timeSlot

import androidx.annotation.VisibleForTesting
import com.greencopper.core.content.serializers.ZonedDateTimeWithInstantSerializer
import com.greencopper.event.timeSlot.data.database.TimeSlotEntity
import com.greencopper.toolkit.extensions.toZonedDateTime
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Serializable
public data class TimeSlot(
    val id: Long,
    val scheduleItemId: Long,
    val dayOfEvent: @Serializable(with = ZonedDateTimeWithInstantSerializer::class) ZonedDateTime,
    val startDate: @Serializable(with = ZonedDateTimeWithInstantSerializer::class) ZonedDateTime? = null,
    val endDate: @Serializable(with = ZonedDateTimeWithInstantSerializer::class) ZonedDateTime? = null,
) : Comparable<TimeSlot> {

    internal constructor(
        id: Long,
        scheduleItemId: Long,
        dayOfEvent: String,
        startDate: String?,
        endDate: String?
    ) : this(
        id,
        scheduleItemId,
        ZonedDateTime.parse(dayOfEvent, dateTimeFormatter),
        startDate.toZonedDateTime(dateTimeFormatter),
        endDate.toZonedDateTime(dateTimeFormatter)
    )

    override fun compareTo(other: TimeSlot): Int {
        val dayOfEventComparison = dayOfEvent.compareTo(other.dayOfEvent)
        if (dayOfEventComparison == 0) {
            startDate ?: return -1
            other.startDate ?: return +1
            val startDateComparison = startDate.compareTo(other.startDate)
            if (startDateComparison == 0) {
                endDate ?: return -1
                other.endDate ?: return +1
                return endDate.compareTo(other.endDate)
            } else {
                return startDateComparison
            }
        }
        return dayOfEventComparison
    }
}

internal val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

@VisibleForTesting
internal val dateTimeFormatterForTesting: DateTimeFormatter = dateTimeFormatter

internal fun TimeSlotEntity.toDataModel(): TimeSlot {
    return TimeSlot(
        id,
        scheduleItemId,
        dayOfEventText,
        startDateText,
        endDateText
    )
}
