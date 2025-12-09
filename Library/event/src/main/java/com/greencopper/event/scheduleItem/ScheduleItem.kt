package com.greencopper.event.scheduleItem

import com.greencopper.core.data.KiboSerializable
import com.greencopper.event.scheduleItem.data.database.ScheduleItemEntity
import com.greencopper.interfacekit.favorites.Favoriteable
import com.greencopper.interfacekit.lists.ListRepository
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class ScheduleItem(
    override val itemId: Long,
    val activityId: Long,
    val stageId: Long? = null,
    val name: String,
    val subtitle: String? = null,
    val description: String? = null,
    val photos: List<String>,
    val tags: List<String> = emptyList(),
    val performerIds: List<String> = emptyList(),
) : Favoriteable<Long>, ListRepository.Item<Long>, KiboSerializable<ScheduleItem> {
    override fun getSerializer(): KSerializer<ScheduleItem> = serializer()
}

internal fun ScheduleItemEntity.toDataModel() =
    ScheduleItem(id, activityId, stageId, name, subtitle, description, photos, tags, performerIds)
