package com.greencopper.core.recipe

import android.content.Context
import com.greencopper.core.content.recipe.RecipeOverride
import java.io.File

internal class CoreConfigRecipeOverride(
    private val context: Context,
    coreConfigurationHolder: CoreConfigurationHolder
): CoreConfigRecipe(coreConfigurationHolder), RecipeOverride {
    override val componentPathOverride = componentPath
    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))
}
