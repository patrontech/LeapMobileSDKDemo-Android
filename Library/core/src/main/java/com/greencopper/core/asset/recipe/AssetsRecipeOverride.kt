package com.greencopper.core.asset.recipe

import android.content.Context
import com.greencopper.core.asset.manager.AssetsManager
import com.greencopper.core.content.recipe.RecipeOverride
import java.io.File

internal class AssetsRecipeOverride(
    private val context: Context,
    assetsManager: AssetsManager,
    assetsConfigurationHolder: AssetsConfigurationHolder,
): AssetsRecipe(assetsManager, assetsConfigurationHolder), RecipeOverride {
    override val componentPathOverride = componentPath

    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))
}
