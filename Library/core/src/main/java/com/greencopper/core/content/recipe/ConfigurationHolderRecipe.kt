package com.greencopper.core.content.recipe

import com.greencopper.core.data.KiboSerializable
import java.io.File

public abstract class ConfigurationHolderRecipe<C: KiboSerializable<C>, H: ConfigurationHolder<C>>(
    protected val configurationHolder: H,
    // Unfortunately these are necessary due to type erasure
    decode: (String) -> C,
    write: C.(File) -> Unit
): ConfigurationRecipe<C>(decode, write) {
    override suspend fun tryToApply(contentDirectory: File) {
        configurationHolder.currentConfiguration.value =
            decode(contentDirectory.config().readText())
    }
}
