package com.greencopper.event.stage.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class StageEntity(
    @PrimaryKey val id: Long = -1,
    val name: String,
    val subtitle: String? = null,
    val photos: List<String>,
    val tags: List<String> = emptyList(),
    val stageDetailLink: String? = null,
    val stageOrder: Int? = null,
)
