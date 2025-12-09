package com.greencopper.event.activity.content

import com.greencopper.event.activity.data.database.ContentActivityEntity
import kotlinx.serialization.Serializable

@Serializable
internal data class ContentActivity(
    val id: Long,
    val name: String,
    val subtitle: String? = null,
    val description: String? = null,
    val photos: List<String>,
    val tags: List<String> = emptyList(),
    val order: Int? = null,
)

internal fun ContentActivity.toEntityModel() =
    ContentActivityEntity(id, name, subtitle, description, photos, tags, order)
