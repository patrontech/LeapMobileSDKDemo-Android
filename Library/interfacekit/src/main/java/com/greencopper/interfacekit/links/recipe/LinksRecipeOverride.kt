package com.greencopper.interfacekit.links.recipe

import android.content.Context
import com.greencopper.core.content.recipe.RecipeOverride
import com.greencopper.interfacekit.links.LinksConfigurationHolder
import java.io.File

internal class LinksRecipeOverride(
    private val context: Context,
    configHolder: LinksConfigurationHolder,
): LinksRecipe(configHolder), RecipeOverride {
    override val componentPathOverride = componentPath
    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))
}
