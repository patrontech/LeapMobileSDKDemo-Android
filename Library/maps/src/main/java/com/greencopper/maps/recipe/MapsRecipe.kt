package com.greencopper.maps.recipe

import com.greencopper.core.content.recipe.ConfigurationRecipe
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.content.recipe.Resettable
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath
import java.io.File

internal open class MapsRecipe(
    private val mapsRepository: MapsRepository
): ConfigurationRecipe<MapsConfiguration>(
    KiboSerializable.Companion::decodeFromString,
    MapsConfiguration::writeToPath
), Resettable {

    override val key: ContentRecipeKey = ContentRecipeKey("Maps", 1, 2)
    override val componentPath: String = "maps"

    override suspend fun tryToApply(contentDirectory: File) {
        mapsRepository.setConfiguration(decode(contentDirectory.config().readText()))
    }

    override fun reset() {
        mapsRepository.clearConfig()
    }
}
