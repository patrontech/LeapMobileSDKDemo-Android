package com.greencopper.core.content.recipe

import com.greencopper.core.data.KiboSerializable
import kotlinx.coroutines.flow.MutableStateFlow

public abstract class ConfigurationHolder<T: KiboSerializable<T>>(
    configuration: T? = null
) {
    public val currentConfiguration: MutableStateFlow<T?> = MutableStateFlow(configuration)
}