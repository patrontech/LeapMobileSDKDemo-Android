package com.greencopper.event.performers.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class PerformerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val subtitle: String? = null,
    val description: String? = null,
    val order: Int? = null,
    val photos: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
)
