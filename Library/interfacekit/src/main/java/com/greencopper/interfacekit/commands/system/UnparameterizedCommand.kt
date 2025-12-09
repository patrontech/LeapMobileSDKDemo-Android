package com.greencopper.interfacekit.commands.system

import com.greencopper.interfacekit.navigation.layout.Layout
import kotlinx.coroutines.flow.Flow

public abstract class UnparameterizedCommand : Command {
    @Throws
    public abstract fun execute(origin: Layout? = null): Flow<Boolean>

    override fun execute(params: CommandParameters?, origin: Layout?): Flow<Boolean> = execute(origin)
}
