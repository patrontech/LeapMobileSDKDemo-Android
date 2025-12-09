package com.greencopper.interfacekit.rootview

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

public class RootViewConfigurationHolder {
    private val configurationFlow: MutableSharedFlow<RootViewConfiguration?> =
        MutableSharedFlow(replay = 1)

    private var currentConfiguration: RootViewConfiguration? = null

    public val value: RootViewConfiguration?
        get() = currentConfiguration

    public val flow: Flow<RootViewConfiguration?>
        get() = configurationFlow

    public fun tryEmit(config: RootViewConfiguration?) {
        currentConfiguration = config
        configurationFlow.tryEmit(config)
    }
}
