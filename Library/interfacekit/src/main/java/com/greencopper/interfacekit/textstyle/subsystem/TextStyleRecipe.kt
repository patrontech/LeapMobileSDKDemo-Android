package com.greencopper.interfacekit.textstyle.subsystem

import com.greencopper.core.content.recipe.ConfigurationRecipe
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath
import java.io.File

internal open class TextStyleRecipe(
    private val textStyleRepository: TextStyleRepository
): ConfigurationRecipe<TextStyleConfiguration>(
    KiboSerializable.Companion::decodeFromString,
    TextStyleConfiguration::writeToPath
) {

    override val key: ContentRecipeKey = ContentRecipeKey("InterfaceKit.TextStyle", 1, 1)
    override val componentPath: String = "interfaceKit/textStyle"

    override suspend fun tryToApply(contentDirectory: File) {
        textStyleRepository.loadTextStyles(decode(contentDirectory.config().readText()))
    }

}
