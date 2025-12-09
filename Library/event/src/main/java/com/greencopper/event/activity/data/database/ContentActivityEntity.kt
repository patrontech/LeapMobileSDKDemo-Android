package com.greencopper.event.activity.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class ContentActivityEntity(
    @PrimaryKey val id: Long = -1L,
    val name: String,
    val subtitle: String? = null,
    val description: String? = null,
    val photos: List<String>,
    val tags: List<String> = emptyList(),
    val activityOrder: Int? = null,
)
