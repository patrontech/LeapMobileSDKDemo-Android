package com.greencopper.event.scheduleItem.content

import com.greencopper.event.scheduleItem.data.database.ScheduleItemEntity
import kotlinx.serialization.Serializable

@Serializable
internal data class ContentScheduleItem(
    val id: Long,
    val activityId: Long,
    val stageId: Long? = null,
    val name: String,
    val subtitle: String? = null,
    val description: String? = null,
    val photos: List<String>,
    val tags: List<String> = emptyList(),
    val performerIds: List<String> = emptyList(),
)

internal fun ContentScheduleItem.toEntityModel() =
    ScheduleItemEntity(id,
        activityId,
        stageId,
        name,
        subtitle,
        description,
        photos,
        tags,
        performerIds)
