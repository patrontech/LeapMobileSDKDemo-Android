package com.greencopper.event.performers

import com.greencopper.core.data.KiboSerializable
import com.greencopper.event.performers.data.database.PerformerEntity
import com.greencopper.interfacekit.lists.ListRepository
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class Performer(
    override val itemId: String,
    val name: String,
    val subtitle: String? = null,
    val description: String? = null,
    val order: Int? = null,
    val photos: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
) : ListRepository.Item<String>, KiboSerializable<Performer> {
    override fun getSerializer(): KSerializer<Performer> = serializer()
}

internal fun PerformerEntity.toDataModel() =
    Performer(
        itemId = id,
        name = name,
        subtitle = subtitle,
        description = description,
        order = order,
        photos = photos,
        tags = tags,
    )
