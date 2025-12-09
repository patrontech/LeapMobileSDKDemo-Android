package com.greencopper.core.services.manager

import com.greencopper.core.content.recipe.ConfigurationHolderRecipe
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath

internal class ServicesConfigurationRecipe(
    configHolder: ServicesConfigurationHolder
): ConfigurationHolderRecipe<ServicesConfiguration, ServicesConfigurationHolder>(
    configHolder,
    KiboSerializable.Companion::decodeFromString,
    ServicesConfiguration::writeToPath
) {
    override val key: ContentRecipeKey = ContentRecipeKey("Core.Services", 1, 1)
    override val componentPath: String = "core/services"
}