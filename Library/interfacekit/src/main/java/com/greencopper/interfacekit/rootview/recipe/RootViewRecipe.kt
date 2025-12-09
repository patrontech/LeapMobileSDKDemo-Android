package com.greencopper.interfacekit.rootview.recipe

import com.greencopper.core.content.recipe.ConfigurationRecipe
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath
import com.greencopper.interfacekit.rootview.RootViewConfiguration
import com.greencopper.interfacekit.rootview.RootViewConfigurationHolder
import java.io.File

internal open class RootViewRecipe(
    private val rootViewConfigurationHolder: RootViewConfigurationHolder
) : ConfigurationRecipe<RootViewConfiguration>(
    KiboSerializable.Companion::decodeFromString,
    RootViewConfiguration::writeToPath
) {
    override val key: ContentRecipeKey = ContentRecipeKey("InterfaceKit.RootView", 1, 1)
    override val componentPath: String = "interfaceKit/rootView"

    override suspend fun tryToApply(contentDirectory: File) {
        val config = decode(contentDirectory.config().readText())
        rootViewConfigurationHolder.tryEmit(config.removeTabBarData())
    }
}
