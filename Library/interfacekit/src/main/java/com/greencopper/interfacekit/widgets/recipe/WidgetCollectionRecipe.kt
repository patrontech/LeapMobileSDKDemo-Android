package com.greencopper.interfacekit.widgets.recipe

import com.greencopper.core.content.recipe.ConfigurationHolderRecipe
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.content.recipe.Resettable
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.WidgetCollectionConfigurationHolder

internal open class WidgetCollectionRecipe(
    widgetCollectionConfigurationHolder: WidgetCollectionConfigurationHolder,
) : ConfigurationHolderRecipe<WidgetCollectionConfiguration, WidgetCollectionConfigurationHolder>(
    widgetCollectionConfigurationHolder,
    KiboSerializable.Companion::decodeFromString,
    WidgetCollectionConfiguration::writeToPath
), Resettable {
    override val key: ContentRecipeKey = ContentRecipeKey("InterfaceKit.WidgetCollection", 1, 1)
    override val componentPath: String = "interfaceKit/widgetCollection"

    override fun reset() {
        configurationHolder.currentConfiguration.value = null
    }
}
