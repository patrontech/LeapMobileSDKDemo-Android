package com.greencopper.core.asset.recipe

import com.greencopper.core.asset.manager.AssetsManager
import com.greencopper.core.content.recipe.*
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath
import java.io.File

internal open class AssetsRecipe(
    private val assetsManager: AssetsManager,
    assetsConfigurationHolder: AssetsConfigurationHolder
) : ConfigurationHolderRecipe<AssetsConfiguration, AssetsConfigurationHolder>(
    assetsConfigurationHolder,
    KiboSerializable.Companion::decodeFromString,
    AssetsConfiguration::writeToPath
), Resettable {
    override val key: ContentRecipeKey = ContentRecipeKey("Core.Assets", 1, 2)
    override val componentPath: String = "core/assets"

    override suspend fun tryToApply(contentDirectory: File) {
        super.tryToApply(contentDirectory)

        assetsManager.cleanUnusedAssets()
        assetsManager.loadMissingAssets()
    }

    override fun reset() {
        configurationHolder.currentConfiguration.value = null
    }
}
