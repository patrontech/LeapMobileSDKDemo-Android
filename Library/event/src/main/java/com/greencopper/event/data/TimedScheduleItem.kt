package com.greencopper.event.data

import com.greencopper.event.data.database.ScheduleItemTimeSlotStageJoined
import com.greencopper.event.scheduleItem.ScheduleItem
import com.greencopper.event.scheduleItem.toDataModel
import com.greencopper.event.stage.Stage
import com.greencopper.event.stage.toDataModel
import com.greencopper.event.timeSlot.TimeSlot
import com.greencopper.event.timeSlot.toDataModel
import kotlinx.serialization.Serializable

@Serializable
public data class TimedScheduleItem(val scheduleItem: ScheduleItem, val timeSlot: TimeSlot, val stage: Stage? = null)

internal fun ScheduleItemTimeSlotStageJoined.toDataModel(): TimedScheduleItem {
    return TimedScheduleItem(
        scheduleItem.toDataModel(),
        timeSlot.toDataModel(),
        stage?.toDataModel()
    )
}
