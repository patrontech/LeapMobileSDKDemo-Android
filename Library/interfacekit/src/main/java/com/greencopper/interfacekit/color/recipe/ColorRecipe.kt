package com.greencopper.interfacekit.color.recipe

import com.greencopper.core.content.recipe.ConfigurationRecipe
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath
import com.greencopper.interfacekit.color.ColorsConfiguration
import com.greencopper.interfacekit.color.repository.ColorRepository
import java.io.File

internal open class ColorRecipe(
    private val colorRepository: ColorRepository,
) : ConfigurationRecipe<ColorsConfiguration>(
    KiboSerializable.Companion::decodeFromString,
    ColorsConfiguration::writeToPath
) {
    override val key: ContentRecipeKey = ContentRecipeKey("InterfaceKit.Color", 1, 1)
    override val componentPath: String = "interfaceKit/color"

    override suspend fun tryToApply(contentDirectory: File) {
        val configuration = decode(contentDirectory.config().readText())
        colorRepository.loadColors(configuration)
    }
}
