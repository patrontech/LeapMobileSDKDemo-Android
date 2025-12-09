package com.greencopper.interfacekit.links.recipe

import com.greencopper.core.content.recipe.ConfigurationHolderRecipe
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.content.recipe.Resettable
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath
import com.greencopper.interfacekit.links.LinksConfiguration
import com.greencopper.interfacekit.links.LinksConfigurationHolder

internal open class LinksRecipe(
    configHolder: LinksConfigurationHolder
) : ConfigurationHolderRecipe<LinksConfiguration, LinksConfigurationHolder>(
    configHolder,
    KiboSerializable.Companion::decodeFromString,
    LinksConfiguration::writeToPath
), Resettable {
    override val key: ContentRecipeKey = ContentRecipeKey("InterfaceKit.Links", 1, 1)
    override val componentPath: String = "interfaceKit/links"

    override fun reset() {
        configurationHolder.currentConfiguration.value = null
    }
}