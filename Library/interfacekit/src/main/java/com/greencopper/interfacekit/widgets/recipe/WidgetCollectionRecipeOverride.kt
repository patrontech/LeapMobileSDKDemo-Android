package com.greencopper.interfacekit.widgets.recipe

import android.content.Context
import com.greencopper.core.content.recipe.RecipeOverride
import com.greencopper.interfacekit.widgets.WidgetCollectionConfigurationHolder
import java.io.File

internal class WidgetCollectionRecipeOverride(
    private val context: Context,
    widgetCollectionConfigurationHolder: WidgetCollectionConfigurationHolder,
): WidgetCollectionRecipe(widgetCollectionConfigurationHolder), RecipeOverride {
    override val componentPathOverride = componentPath
    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))
}
