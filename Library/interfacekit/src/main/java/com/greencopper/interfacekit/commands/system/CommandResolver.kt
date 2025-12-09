package com.greencopper.interfacekit.commands.system

import com.greencopper.toolkit.di.binding.Creator
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.container.Key

public interface CommandResolver {
    public fun resolve(key: CommandInfo.Key): Command?
}

public inline fun <reified T : Command> Registrar.bindCommand(
    key: CommandInfo.Key,
    noinline creator: Creator<T>,
): Key = this.bindProvider<Command>(key, creator)
