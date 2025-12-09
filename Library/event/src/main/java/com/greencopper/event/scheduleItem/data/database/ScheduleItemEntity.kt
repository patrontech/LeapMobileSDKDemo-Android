package com.greencopper.event.scheduleItem.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class ScheduleItemEntity(
    @PrimaryKey val id: Long = -1L,
    val activityId: Long,
    val stageId: Long? = null,
    val name: String,
    val subtitle: String? = null,
    val description: String? = null,
    val photos: List<String>,
    val tags: List<String> = emptyList(),
    val performerIds: List<String> = emptyList(),
)
