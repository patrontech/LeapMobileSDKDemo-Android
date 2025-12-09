package com.greencopper.interfacekit.rootview.recipe

import android.content.Context
import com.greencopper.core.content.recipe.RecipeOverride
import com.greencopper.interfacekit.rootview.RootViewConfigurationHolder
import java.io.File

internal class RootViewRecipeOverride(
    private val context: Context,
    rootViewConfigurationHolder: RootViewConfigurationHolder,
): RootViewRecipe(rootViewConfigurationHolder), RecipeOverride {
    override val componentPathOverride = componentPath
    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))
}
