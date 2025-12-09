package com.greencopper.interfacekit.textstyle.subsystem

import android.content.Context
import com.greencopper.core.content.recipe.RecipeOverride
import java.io.File

internal class TextStyleRecipeOverride(
    private val context: Context,
    textStyleRepository: TextStyleRepository,
): TextStyleRecipe(textStyleRepository), RecipeOverride {
    override val componentPathOverride = componentPath
    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))
}
