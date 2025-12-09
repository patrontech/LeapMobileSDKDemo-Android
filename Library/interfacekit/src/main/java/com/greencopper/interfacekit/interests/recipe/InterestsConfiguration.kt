package com.greencopper.interfacekit.interests.recipe

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class InterestsConfiguration(val interests: List<Interest>) : KiboSerializable<InterestsConfiguration> {
    override fun getSerializer(): KSerializer<InterestsConfiguration> = serializer()
}

@Serializable
public data class Interest(
    val id: String,
    val name: String,
    val order: Int,
    val analyticsName: String,
    val tags: List<String> = emptyList(),
)
