package com.greencopper.core.services.iplocation

import com.greencopper.core.content.recipe.ConfigurationHolderRecipe
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath

internal class IPLocationConfigRecipe(
    ipLocationConfigurationHolder: IPLocationConfigurationHolder
): ConfigurationHolderRecipe<IPLocationConfiguration, IPLocationConfigurationHolder>(
    ipLocationConfigurationHolder,
    KiboSerializable.Companion::decodeFromString,
    IPLocationConfiguration::writeToPath
) {
    override val key: ContentRecipeKey = ContentRecipeKey("Core.IPLocationService", 1, 1)
    override val componentPath: String = "core/iplocation"
}
