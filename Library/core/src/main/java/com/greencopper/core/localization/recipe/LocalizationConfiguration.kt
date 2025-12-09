package com.greencopper.core.localization.recipe

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class LocalizationConfiguration(
    val defaultLocale: String,
    val locales: List<String>
): KiboSerializable<LocalizationConfiguration> {
    override fun getSerializer(): KSerializer<LocalizationConfiguration> = serializer()
}