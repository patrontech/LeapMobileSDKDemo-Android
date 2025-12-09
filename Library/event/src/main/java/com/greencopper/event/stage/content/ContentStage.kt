package com.greencopper.event.stage.content

import com.greencopper.event.stage.data.StageEntity
import kotlinx.serialization.Serializable

@Serializable
internal data class ContentStage(
    val id: Long = -1,
    val name: String,
    val subtitle: String? = null,
    val photos: List<String>,
    val tags: List<String> = emptyList(),
    val stageDetailLink: String? = null,
    val order: Int? = null,
)

internal fun ContentStage.toEntityModel() =
    StageEntity(
        id = id,
        name = name,
        subtitle = subtitle,
        photos = photos,
        tags = tags,
        stageDetailLink = stageDetailLink,
        stageOrder = order,
    )
