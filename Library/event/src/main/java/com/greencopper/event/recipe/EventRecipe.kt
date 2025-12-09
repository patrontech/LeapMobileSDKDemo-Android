package com.greencopper.event.recipe

import androidx.annotation.VisibleForTesting
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.content.recipe.TryContentRecipe
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath
import com.greencopper.event.EventDataProcessor
import java.io.File

internal open class EventRecipe(
    private val eventDataProcessor: EventDataProcessor,
    private val eventConfigurationHolder: EventConfigurationHolder,
) : TryContentRecipe {
    override val key: ContentRecipeKey = ContentRecipeKey("Event", 1, 9)
    override val componentPath: String = "event/"

    override suspend fun tryToProcess(unarchivedDirectory: File, contentDirectory: File) {
        require(unarchivedDirectory.data().exists()) { "Data folder doesn't exist" }
        eventDataProcessor.process(unarchivedDirectory.data(), contentDirectory.data())

        if (unarchivedDirectory.config().exists()) {
            val eventConfiguration = KiboSerializable.decodeFromString<EventConfiguration>(
                unarchivedDirectory.config().readText()
            )
            eventConfiguration.writeToPath(contentDirectory.config())
        }
    }

    override suspend fun tryToApply(contentDirectory: File) {
        eventDataProcessor.apply(contentDirectory.data())

        if (contentDirectory.config().exists()) {
            eventConfigurationHolder.currentConfiguration.value = KiboSerializable.decodeFromString(
                contentDirectory.config().readText()
            )
        }
    }
}

@VisibleForTesting
internal fun File.data(): File = File(this, "data")
