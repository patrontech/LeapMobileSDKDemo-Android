package com.greencopper.core.location.recipe

import com.greencopper.core.content.recipe.ConfigurationHolderRecipe
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.content.recipe.Resettable
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath
import com.greencopper.core.location.LocationConfigurationHolder

internal open class LocationRecipe(
    configurationHolder: LocationConfigurationHolder
) : ConfigurationHolderRecipe<LocationConfiguration, LocationConfigurationHolder>(
    configurationHolder,
    KiboSerializable.Companion::decodeFromString,
    LocationConfiguration::writeToPath
), Resettable {

    override val key: ContentRecipeKey = ContentRecipeKey("Core.Location", 1, 1)
    override val componentPath: String = "core/location"

    override fun reset() {
        configurationHolder.currentConfiguration.value = null
    }
}