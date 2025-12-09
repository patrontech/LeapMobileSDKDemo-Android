package com.greencopper.core.recipe

import com.greencopper.core.content.recipe.*
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath

internal open class CoreConfigRecipe(
    coreConfigurationHolder: CoreConfigurationHolder
): ConfigurationHolderRecipe<CoreConfiguration, CoreConfigurationHolder>(
    coreConfigurationHolder,
    KiboSerializable.Companion::decodeFromString,
    CoreConfiguration::writeToPath
), Resettable {
    override val key: ContentRecipeKey = ContentRecipeKey("Core", 1, 1)
    override val componentPath: String = "core/"

    override fun reset() {
        configurationHolder.currentConfiguration.value = null
    }
}
