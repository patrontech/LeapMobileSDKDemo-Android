package com.greencopper.thuzi.fanscan

import kotlinx.serialization.Serializable

@Serializable
internal data class FanscanCheckIn(
    internal val checkInUrl: String,
    internal val moduleId: String,
    internal val project: String,
    internal var attempts: Int = 0,
    internal val date: Long = System.currentTimeMillis()
)