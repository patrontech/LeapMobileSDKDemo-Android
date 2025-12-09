package com.greencopper.event.stage

import com.greencopper.event.stage.data.StageEntity
import kotlinx.serialization.Serializable

@Serializable
public data class Stage(
    val id: Long,
    val name: String,
    val subtitle: String? = null,
    val photos: List<String>,
    val tags: List<String> = emptyList(),
    val stageDetailLink: String? = null,
    val order: Int? = null,
)

internal fun StageEntity.toDataModel() =
    Stage(
        id = id,
        name = name,
        subtitle = subtitle,
        photos = photos,
        tags = tags,
        stageDetailLink = stageDetailLink,
        order = stageOrder,
    )
