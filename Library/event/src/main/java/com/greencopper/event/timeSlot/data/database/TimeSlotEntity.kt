package com.greencopper.event.timeSlot.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class TimeSlotEntity(
    @PrimaryKey val id: Long = -1,
    val scheduleItemId: Long,
    @ColumnInfo(name = "dayOfEvent") val dayOfEventText: String,
    @ColumnInfo(name = "startDate") val startDateText: String? = null,
    @ColumnInfo(name = "endDate") val endDateText: String? = null
)