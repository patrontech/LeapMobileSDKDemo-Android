package com.greencopper.event.data.database

import androidx.room.Embedded
import androidx.room.Relation
import com.greencopper.event.scheduleItem.data.database.ScheduleItemEntity
import com.greencopper.event.stage.data.StageEntity
import com.greencopper.event.timeSlot.data.database.TimeSlotEntity

internal data class ScheduleItemTimeSlotStageJoined(
    @Embedded
    val scheduleItem: ScheduleItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "scheduleItemId"
    )
    val timeSlot: TimeSlotEntity,
    @Relation(
        parentColumn = "stageId",
        entityColumn = "id"
    )
    val stage: StageEntity? = null,
)
