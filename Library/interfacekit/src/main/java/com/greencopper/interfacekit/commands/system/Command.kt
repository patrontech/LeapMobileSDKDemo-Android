package com.greencopper.interfacekit.commands.system

import com.greencopper.interfacekit.navigation.layout.Layout
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement

public typealias CommandParameters = JsonElement

/**
 * Shouldn't be used directly, you need to select
 * ParameterizedCommand or UnparameterizedCommand
 * to override
 */
public interface Command {
    @Throws
    public fun execute(params: CommandParameters?, origin: Layout? = null): Flow<Boolean>
}
