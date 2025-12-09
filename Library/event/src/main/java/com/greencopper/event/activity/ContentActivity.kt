package com.greencopper.event.activity

import com.greencopper.event.activity.data.database.ContentActivityEntity
import com.greencopper.interfacekit.lists.ListRepository

public data class ContentActivity(
    override val itemId: Long,
    val name: String,
    val subtitle: String? = null,
    val description: String? = null,
    val photos: List<String>,
    val tags: List<String> = emptyList(),
    val order: Int? = null,
) : ListRepository.Item<Long>

internal fun ContentActivityEntity.toDataModel() =
    ContentActivity(
        itemId = id,
        name = name,
        subtitle = subtitle,
        description = description,
        photos = photos,
        tags = tags,
        order = activityOrder,
    )
