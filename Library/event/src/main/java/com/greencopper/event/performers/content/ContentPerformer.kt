package com.greencopper.event.performers.content

import com.greencopper.event.performers.data.database.PerformerEntity
import kotlinx.serialization.Serializable

@Serializable
internal data class ContentPerformer(
    val id: String,
    val name: String,
    val subtitle: String? = null,
    val description: String? = null,
    val order: Int? = null,
    val photos: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
)

internal fun ContentPerformer.toEntityModel() =
    PerformerEntity(id, name, subtitle, description, order, photos, tags)
