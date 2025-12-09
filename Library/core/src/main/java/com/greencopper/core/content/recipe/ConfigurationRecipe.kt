package com.greencopper.core.content.recipe

import com.greencopper.core.data.KiboSerializable
import java.io.File

public abstract class ConfigurationRecipe<C: KiboSerializable<C>>(
    // Unfortunately these are necessary due to type erasure
    protected val decode: (String) -> C,
    protected val write: C.(File) -> Unit
): TryContentRecipe {
    override suspend fun tryToProcess(unarchivedDirectory: File, contentDirectory: File) {
        require(unarchivedDirectory.isDirectory) { "Unarchived directory is not a proper directory" }
        require(unarchivedDirectory.config().exists()) { "No config file found." }

        val config = decode(unarchivedDirectory.config().readText())
        config.write(contentDirectory.config())
    }
}
