package com.greencopper.interfacekit.interests.recipe

import com.greencopper.core.content.recipe.ConfigurationHolderRecipe
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath

internal open class InterestsRecipe(
    configHolder: InterestsConfigurationHolder,
) : ConfigurationHolderRecipe<InterestsConfiguration, InterestsConfigurationHolder>(
    configHolder,
    KiboSerializable.Companion::decodeFromString,
    InterestsConfiguration::writeToPath,
) {
    override val key = ContentRecipeKey("InterfaceKit.Interests", 1, 1)
    override val componentPath: String = "interfaceKit/interests"
}
