package com.greencopper.core.location

import com.greencopper.core.remotestate.RemoteStateEntry
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

@Serializable
internal data class RegionsRemoteStateEntry(
    val regions: Set<Int>
) : RemoteStateEntry(
    key = "regions",
    value = JsonArray(
        regions.map { JsonPrimitive(it) }
    ),
    isUrgent = true
)