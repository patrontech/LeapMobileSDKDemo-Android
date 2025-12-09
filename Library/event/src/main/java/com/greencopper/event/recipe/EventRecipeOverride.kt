package com.greencopper.event.recipe

import android.content.Context
import com.greencopper.core.content.recipe.RecipeOverride
import com.greencopper.event.EventDataProcessor
import java.io.File

/**
 * This RecipeOverride overrides the process function, meaning that we'd need to clear data
 * to take changes in the override files into account
 */
internal class EventRecipeOverride(
    context: Context,
    eventDataProcessor: EventDataProcessor,
    eventConfigurationHolder: EventConfigurationHolder,
): EventRecipe(eventDataProcessor, eventConfigurationHolder), RecipeOverride {
    override val componentPathOverride = componentPath
    private val overrideFolder = overrideFolder(context)

    override suspend fun tryToProcess(unarchivedDirectory: File, contentDirectory: File) {
        super.tryToProcess(overrideFolder, overrideFolder)
    }

    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder)
}
